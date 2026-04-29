package top.fifthlight.fastmerger.scanner;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import top.fifthlight.bazel.worker.api.Worker;
import top.fifthlight.fastmerger.bindeps.BindepsConstants;
import top.fifthlight.fastmerger.bindeps.BindepsWriter;
import top.fifthlight.fastmerger.scanner.classdeps.ClassDepsScanner;
import top.fifthlight.fastmerger.scanner.classdeps.ClassInfo;
import top.fifthlight.fastmerger.scanner.pathmap.PathMap;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.Manifest;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Parameters;

public class ScannerWorker extends Worker {
    private static class Environment {
        private final Path inputPath;
        private final Path outputPath;
        private boolean scanned = false;

        private Environment(Path inputPath, Path outputPath) {
            this.inputPath = inputPath;
            this.outputPath = outputPath;
        }

        private record ClassInfoKey(PathMap.Entry nameKey, int release) {
            @Override
            public String toString() {
                return "ClassInfoKey[fullName=" + nameKey.fullName() + ", release=" + release + "]";
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof ClassInfoKey(PathMap.Entry otherNameKey, int otherRelease))) return false;
                return release == otherRelease && Objects.equals(nameKey, otherNameKey);
            }

            @Override
            public int hashCode() {
                return Objects.hash(nameKey, release);
            }
        }

        private record ClassInfoEntry(ClassInfo classInfo, int release, ResourceInfo resourceInfo) {
        }

        private final PathMap pathMap = new PathMap();
        private final Map<String, ResourceInfo> resourceInfoNameMap = new HashMap<>();
        private final Map<ClassInfoKey, ClassInfoEntry> classInfoNameMap = new HashMap<>();

        private static int getFlag(ZipArchiveEntry entry, String entryName) {
            var flag = 0;
            if (entryName.endsWith(".class")) {
                flag = flag | BindepsConstants.RESOURCE_FLAG_CLASS;
            } else {
                flag = flag | BindepsConstants.RESOURCE_FLAG_RESOURCE;
                var isMetaInfo = entryName.startsWith("META-INF/");
                var isAndroidAarRule = entry.getName().equals("proguard.txt");
                var isProguardFile = entry.getName().startsWith("META-INF/proguard/") && entry.getName().endsWith(".pro");
                var isSignature = entryName.endsWith(".SF") || entryName.endsWith(".RSA") || entryName.endsWith(".DSA");
                if (entryName.equals("META-INF/MANIFEST.MF")) {
                    flag = flag | BindepsConstants.RESOURCE_FLAG_MANIFEST;
                } else if (entryName.startsWith("META-INF/services/")) {
                    flag = flag | BindepsConstants.RESOURCE_FLAG_SPI;
                    flag = flag | BindepsConstants.RESOURCE_FLAG_INLINE;
                } else if (isAndroidAarRule || isProguardFile) {
                    flag = flag | BindepsConstants.RESOURCE_FLAG_PROGUARD;
                    flag = flag | BindepsConstants.RESOURCE_FLAG_INLINE;
                } else if (isMetaInfo && isSignature) {
                    flag = flag | BindepsConstants.RESOURCE_FLAG_SIGNATURE;
                }
            }
            return flag;
        }

        private ResourceInfo scanResource(ZipFile zipFile, ZipArchiveEntry entry) throws IOException {
            var entryName = entry.getName();
            var flag = getFlag(entry, entryName);
            var name = pathMap.getOrCreate(entryName);
            var crc32 = (int) entry.getCrc();

            var entryMethod = entry.getMethod();
            var compressMethod = switch (entry.getMethod()) {
                case ZipArchiveEntry.STORED, ZipArchiveEntry.DEFLATED -> (short) entryMethod;
                default -> throw new IllegalStateException("Unsupported compress method " +
                        entryMethod + " for entry: " + entry.getName());
            };

            int dataOffset;
            int compressedSize;
            int uncompressedSize;
            byte[] data = null;
            if ((flag & BindepsConstants.RESOURCE_FLAG_INLINE) == 0) {
                var dataOffsetLong = entry.getDataOffset();
                if (dataOffsetLong > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Data offset too large for entry: " + entry.getName());
                }
                dataOffset = (int) dataOffsetLong;

                var compressedSizeLong = entry.getCompressedSize();
                if (compressedSizeLong > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Compressed size too large for entry: " + entry.getName());
                }
                compressedSize = (int) compressedSizeLong;

                var uncompressedSizeLong = entry.getSize();
                if (uncompressedSizeLong > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Uncompressed size too large for entry: " + entry.getName());
                }
                uncompressedSize = (int) uncompressedSizeLong;
            } else {
                dataOffset = -1;

                var uncompressedSizeLong = entry.getSize();
                if (uncompressedSizeLong > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Uncompressed size too large for entry: " + entry.getName());
                }
                compressedSize = (int) uncompressedSizeLong;
                uncompressedSize = (int) uncompressedSizeLong;

                try (var stream = zipFile.getInputStream(entry)) {
                    data = stream.readNBytes(uncompressedSize);
                }
            }

            return new ResourceInfo(flag, name, crc32, dataOffset, compressedSize, uncompressedSize, compressMethod, data);
        }

        private static final String multiReleasePrefix = "META-INF/versions/";

        private ClassInfoEntry scanClassInfo(ResourceInfo resourceInfo, byte[] entry) {
            var classInfo = ClassDepsScanner.scan(pathMap, entry);
            var release = -1;
            var resourcePath = resourceInfo.name().fullName();
            if (resourcePath.startsWith(multiReleasePrefix)) {
                var builder = new StringBuilder();
                for (var i = multiReleasePrefix.length(); i < resourcePath.length(); i++) {
                    var ch = resourcePath.charAt(i);
                    if (!Character.isDigit(ch)) {
                        break;
                    }
                    builder.append(ch);
                }
                release = Integer.parseInt(builder.toString());
            }
            return new ClassInfoEntry(classInfo, release, resourceInfo);
        }

        private void scanJar() throws Exception {
            var maxConcurrency = Runtime.getRuntime().availableProcessors();
            try (var executor = new ThreadPoolExecutor(
                    maxConcurrency,
                    maxConcurrency,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(maxConcurrency),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            )) {
                try (var zipFile = ZipFile.builder().setPath(inputPath).get()) {
                    var futures = new ArrayList<CompletableFuture<ClassInfoEntry>>();
                    Manifest manifest = null;
                    var entries = zipFile.getEntriesInPhysicalOrder();
                    while (entries.hasMoreElements()) {
                        var entry = entries.nextElement();
                        if (entry.isDirectory()) {
                            continue;
                        }

                        var resourceEntry = scanResource(zipFile, entry);
                        resourceInfoNameMap.put(resourceEntry.name().fullName(), resourceEntry);

                        var name = entry.getName();
                        if (name.equals("META-INF/MANIFEST.MF")) {
                            manifest = new Manifest(zipFile.getInputStream(entry));
                        } else if (name.endsWith(".class")) {
                            byte[] content;
                            try (var stream = zipFile.getInputStream(entry)) {
                                content = stream.readAllBytes();
                            }
                            futures.add(CompletableFuture.supplyAsync(() -> scanClassInfo(resourceEntry, content), executor));
                        }
                    }

                    var isMultiReleaseJar = manifest != null && "true".equalsIgnoreCase(manifest.getMainAttributes().getValue("Multi-Release"));

                    for (var future : futures) {
                        var entry = future.get();
                        var release = isMultiReleaseJar ? entry.release() : -1;
                        var key = new ClassInfoKey(entry.classInfo.entry(), release);
                        if (classInfoNameMap.containsKey(key)) {
                            throw new IllegalStateException("Duplicate class entry: " + key);
                        } else {
                            classInfoNameMap.put(key, entry);
                        }
                    }
                } finally {
                    executor.shutdownNow();
                }
            }
        }

        private ArrayList<PathMap.Entry> pathMapEntries;
        private Object2IntOpenHashMap<PathMap.Entry> pathMapEntriesIndexMap;

        private void sortPathMap() {
            var pathMapResult = pathMap.finish();
            pathMapEntries = new ArrayList<>(pathMapResult.size());
            pathMapEntriesIndexMap = new Object2IntOpenHashMap<>(pathMapResult.size());
            pathMapEntriesIndexMap.defaultReturnValue(-1);
            var stack = new ArrayDeque<PathMap.Entry>();
            pathMapResult.rootEntries().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                    .forEachOrdered(entry -> stack.push(entry.getValue()));

            while (!stack.isEmpty()) {
                var entry = stack.pop();
                pathMapEntriesIndexMap.put(entry, pathMapEntries.size());
                pathMapEntries.add(entry);
                entry.entries().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                        .forEachOrdered(e -> stack.push(e.getValue()));
            }
        }

        private void writeStringMap(BindepsWriter writer) {
            for (var entry : pathMapEntries) {
                var parent = entry.parentEntry();
                var parentIndex = parent != null ? pathMapEntriesIndexMap.getInt(parent.fullName()) : -1;
                writer.writeStringPoolEntry(entry.hash(), parentIndex, entry.nameBytes(), entry.fullNameBytes());
            }
        }

        private ArrayList<ResourceInfo> resourceInfos;
        private Object2IntOpenHashMap<ResourceInfo> resourceInfoIndexMap;

        private void sortResourceInfos() {
            resourceInfos = new ArrayList<>(resourceInfoNameMap.size());
            resourceInfoIndexMap = new Object2IntOpenHashMap<>(resourceInfoNameMap.size());
            resourceInfoIndexMap.defaultReturnValue(-1);
            resourceInfoNameMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(entry -> {
                        resourceInfoIndexMap.put(entry.getValue(), resourceInfos.size());
                        resourceInfos.add(entry.getValue());
                    });
        }

        private void writeResourceInfos(BindepsWriter writer) {
            for (var resourceInfo : resourceInfos) {
                var nameIndex = pathMapEntriesIndexMap.getInt(resourceInfo.name());
                if (nameIndex == -1) {
                    throw new IllegalStateException("Name not found for " + resourceInfo.name().fullName());
                }
                writer.writeResourceEntry(resourceInfo.flag(), nameIndex, resourceInfo.crc32(),
                        resourceInfo.dataOffset(), resourceInfo.compressedSize(), resourceInfo.uncompressedSize(),
                        resourceInfo.compressMethod(), resourceInfo.data());
            }
        }

        private int[] lookupClassEntries(Object2IntMap<PathMap.Entry> entryMap, PathMap.Entry[] entries) {
            return Arrays.stream(entries)
                    .mapToInt(entry -> {
                        var index = entryMap.getInt(entry);
                        if (index == -1) {
                            throw new IllegalStateException("Entry not found for " + entry.fullName());
                        }
                        return index;
                    })
                    .sorted()
                    .toArray();
        }

        private record ClassSortEntry(int entryIndex, int release, ClassInfo classInfo,
                                      ResourceInfo resourceInfo) implements Comparable<ClassSortEntry> {
            @Override
            public int compareTo(ClassSortEntry o) {
                var result = Integer.compare(entryIndex, o.entryIndex);
                // Compare in reverse, so latest release appears first
                result = result == 0 ? Integer.compare(o.release, release) : result;
                return result;
            }
        }

        private void writeClassInfo(BindepsWriter writer) {
            classInfoNameMap.entrySet().stream()
                    .map(entry -> new ClassSortEntry(
                            pathMapEntriesIndexMap.getInt(entry.getKey().nameKey()),
                            entry.getKey().release,
                            entry.getValue().classInfo(),
                            entry.getValue().resourceInfo))
                    .sorted()
                    .forEachOrdered(entry -> {
                        var resourceInfo = entry.resourceInfo();
                        var resourceIndex = resourceInfoIndexMap.getInt(resourceInfo);
                        if (resourceIndex == -1) {
                            throw new IllegalStateException("Resource index not found for " + resourceInfo.name().fullName());
                        }

                        var classInfo = entry.classInfo();
                        var superEntry = classInfo.superClass();
                        var superIndex = (superEntry != null) ? pathMapEntriesIndexMap.getInt(superEntry) : -1;

                        var interfaces = lookupClassEntries(pathMapEntriesIndexMap, classInfo.interfaces());
                        var annotations = lookupClassEntries(pathMapEntriesIndexMap, classInfo.annotations());
                        var dependencies = lookupClassEntries(pathMapEntriesIndexMap, classInfo.dependencies());

                        writer.writeClassInfoEntry(entry.entryIndex(), superIndex, classInfo.accessFlag(),
                                resourceIndex, entry.release(), interfaces, annotations, dependencies);
                    });
        }

        private void writeOutput() throws IOException {
            sortPathMap();
            sortResourceInfos();
            try (var writer = new BindepsWriter(outputPath, pathMapEntries.size(), resourceInfos.size(), classInfoNameMap.size())) {
                writeStringMap(writer);
                writeResourceInfos(writer);
                writeClassInfo(writer);
            }
        }

        public void scan() throws Exception {
            if (scanned) {
                throw new IllegalStateException("Already scanned");
            }
            scanned = true;

            scanJar();
            writeOutput();
        }
    }

    @Override
    protected int handleRequest(@NonNull PrintWriter out, @Nullable Path sandboxDir, String... args) {
        var wrapper = new Handler(sandboxDir);
        var commandLine = new CommandLine(wrapper);
        commandLine.setOut(out);
        commandLine.setErr(out);
        return commandLine.execute(args);
    }

    @Command(name = "scanner", mixinStandardHelpOptions = true)
    private static class Handler implements Callable<Integer> {
        @Parameters(index = "0", description = "JAR file to be scanned")
        Path inputFile;

        @Parameters(index = "1", description = "Output binary dependencies file")
        Path outputFile;

        private final @Nullable Path sandboxDir;

        public Handler(@Nullable Path sandboxDir) {
            this.sandboxDir = sandboxDir;
        }

        @Override
        public Integer call() throws Exception {
            var inputPath = sandboxDir != null ? sandboxDir.resolve(inputFile) : inputFile;
            var outputPath = sandboxDir != null ? sandboxDir.resolve(outputFile) : outputFile;
            var environment = new Environment(inputPath, outputPath);
            environment.scan();
            return 0;
        }
    }

    public static void main(String[] args) throws Exception {
        new ScannerWorker().run(args);
    }
}
