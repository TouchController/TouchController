package top.fifthlight.fabazel.remapper;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.extension.mixin.MixinExtension;
import org.jspecify.annotations.NonNull;
import top.fifthlight.bazel.worker.api.Worker;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;

public class TinyRemapperWorker extends Worker implements AutoCloseable {
    private static final long DOS_EPOCH = 315532800000L;

    private static void setJarEntryTime(JarEntry entry) {
        entry.setCreationTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastAccessTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastModifiedTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setTimeLocal(LocalDateTime.ofEpochSecond(DOS_EPOCH / 1000, 0, ZoneOffset.UTC));
    }

    public static void main(String[] args) throws Exception {
        try (var worker = new TinyRemapperWorker()) {
            worker.run(args);
        }
    }

    private final MappingManager mappings = new MappingManager();
    private static final Pattern MC_LV_PATTERN = Pattern.compile("\\$\\$\\d+");

    private static String bytesToHex(byte[] bytes) {
        var sb = new StringBuilder();
        for (var b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String hashFile(Path file) throws NoSuchAlgorithmException, IOException {
        var digest = MessageDigest.getInstance("SHA-256");
        try (var channel = Files.newByteChannel(file)) {
            var buffer = ByteBuffer.allocate(4096);
            while (channel.read(buffer) >= 0) {
                buffer.flip();
                var array = buffer.array();
                digest.update(array, 0, buffer.limit());
                buffer.clear();
            }
        }
        return bytesToHex(digest.digest());
    }

    @Override
    protected int handleRequest(@NonNull PrintWriter out, @NonNull Path sandboxDir, String... args) {
        try {
            List<String> parameters = new ArrayList<>();
            List<String> arguments = new ArrayList<>();

            for (var arg : args) {
                if (arg.startsWith("--")) {
                    parameters.add(arg);
                } else {
                    arguments.add(arg);
                }
            }

            var mixin = false;
            var fixPackageAccess = false;
            var remapAccessWidener = false;
            var removeJarInJar = false;
            String accessWidenerSourceNamespace = null;

            for (var parameter : parameters) {
                var name = parameter.substring(2); // removePrefix("--")
                var accessWidenerSourceArg = "access_widener_from_namespace_";
                if (name.startsWith(accessWidenerSourceArg)) {
                    accessWidenerSourceNamespace = name.substring(accessWidenerSourceArg.length());
                    continue;
                }
                switch (name) {
                    case "mixin":
                        mixin = true;
                        break;
                    case "fix_package_access":
                        fixPackageAccess = true;
                        break;
                    case "remap_access_widener":
                        remapAccessWidener = true;
                        break;
                    case "remove_jar_in_jar":
                        removeJarInJar = true;
                        break;
                }
            }

            if (arguments.size() < 5) {
                out.println("Bad count of arguments: " + arguments.size() + ", at least 5");
                return 1;
            }

            var inputJar = arguments.get(0);
            var outputJar = arguments.get(1);
            var mappingPath = sandboxDir.resolve(Paths.get(arguments.get(2)));
            var fromNamespace = arguments.get(3);
            var toNamespace = arguments.get(4);

            var classpath = arguments.subList(5, arguments.size())
                    .stream()
                    .map(first -> sandboxDir.resolve(sandboxDir.resolve(Paths.get(first))))
                    .toList();

            if (accessWidenerSourceNamespace == null || accessWidenerSourceNamespace.isEmpty()) {
                accessWidenerSourceNamespace = fromNamespace;
            }

            var mappingArgument = new MappingManager.Argument(
                    mappingPath,
                    hashFile(mappingPath),
                    fromNamespace,
                    toNamespace
            );

            var entry = mappings.get(mappingArgument);

            var logger = new PrintLogger(out);

            var builder = TinyRemapper.newRemapper(logger)
                    .withMappings(entry.getProvider())
                    .renameInvalidLocals(true)
                    .rebuildSourceFilenames(true)
                    .invalidLvNamePattern(MC_LV_PATTERN)
                    .resolveMissing(true)
                    .inferNameFromSameLvIndex(true);
            if (mixin) {
                builder.extension(new MixinExtension());
            }
            if (fixPackageAccess) {
                builder.fixPackageAccess(true);
                builder.checkPackageAccess(true);
            }

            var remapper = builder.build();

            var input = sandboxDir.resolve(Paths.get(inputJar));
            var outputTempFs = Jimfs.newFileSystem(Configuration.unix());
            var outputTempRoot = outputTempFs.getPath("/");
            try {
                var outputBuilder = new OutputConsumerPath.Builder(outputTempRoot);
                outputBuilder.assumeArchive(false);
                var output = outputBuilder.build();

                var nonClassFilesProcessors = new ArrayList<OutputConsumerPath.ResourceRemapper>();
                if (removeJarInJar) {
                    nonClassFilesProcessors.add(JarInJarRemover.INSTANCE);
                }
                nonClassFilesProcessors.addAll(NonClassCopyMode.FIX_META_INF.remappers);
                if (remapAccessWidener) {
                    nonClassFilesProcessors.add(
                            new AccessWidenerRemapper(
                                    entry.getRemapper(),
                                    accessWidenerSourceNamespace,
                                    toNamespace
                            )
                    );
                }

                output.addNonClassFiles(input, remapper, nonClassFilesProcessors);
                remapper.readInputs(input);
                for (var cp : classpath) {
                    remapper.readClassPath(cp);
                }
                remapper.apply(output);
                output.close();
            } finally {
                remapper.finish();
            }

            try (var outputJarStream = new JarOutputStream(Files.newOutputStream(sandboxDir.resolve(Paths.get(outputJar))));
                 var outputFilePaths = Files.walk(outputTempRoot)) {
                outputFilePaths
                        .sorted()
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            var jarEntry = new JarEntry(outputTempRoot.relativize(path).toString());
                            setJarEntryTime(jarEntry);
                            try {
                                outputJarStream.putNextEntry(jarEntry);
                                Files.copy(path, outputJarStream);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
            } finally {
                outputTempFs.close();
            }

            return 0;
        } catch (Exception ex) {
            ex.printStackTrace(out);
            return 1;
        }
    }

    @Override
    public void close() {
        mappings.close();
    }
}
