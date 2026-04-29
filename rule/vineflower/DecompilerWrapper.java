package top.fifthlight.fabazel.decompiler;

import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import net.fabricmc.loom.decompilers.vineflower.TinyJavadocProvider;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import top.fifthlight.bazel.worker.api.Worker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class DecompilerWrapper extends Worker {
    @Override
    protected int handleRequest(@NonNull PrintWriter out, @Nullable Path sandboxDir, String... args) {
        var wrapper = new Handler(sandboxDir, out);
        var commandLine = new CommandLine(wrapper);
        commandLine.setOut(out);
        commandLine.setErr(out);
        return commandLine.execute(args);
    }

    @Command(name = "decompiler", mixinStandardHelpOptions = true)
    public static class Handler implements Callable<Integer> {
        private static final long DOS_EPOCH = 315532800000L;

        private static void setJarEntryTime(JarEntry entry) {
            entry.setCreationTime(FileTime.fromMillis(DOS_EPOCH));
            entry.setLastAccessTime(FileTime.fromMillis(DOS_EPOCH));
            entry.setLastModifiedTime(FileTime.fromMillis(DOS_EPOCH));
            entry.setTimeLocal(LocalDateTime.ofEpochSecond(DOS_EPOCH / 1000, 0, ZoneOffset.UTC));
        }

        private final @Nullable Path sandboxPath;
        private final PrintWriter out;

        public Handler(@Nullable Path sandboxPath, PrintWriter out) {
            this.sandboxPath = sandboxPath;
            this.out = out;
        }

        @Option(names = {"-m", "--mappings"}, description = "Mappings file, must be in tiny format") File mappings;

        @Parameters(index = "0", description = "Output jar file") File outputFile;

        @Parameters(index = "1..*", description = "Input jar files to decompile") File[] inputFiles;

        private static class InMemorySaver implements IResultSaver {
            public final ConcurrentHashMap<String, String> classes = new ConcurrentHashMap<>();

            @Override
            public void saveFolder(String path) {}

            @Override
            public void copyFile(String source, String path, String entryName) {}

            @Override
            public void saveClassFile(
                String path, String qualifiedName, String entryName, String content, int[] mapping) {}

            @Override
            public void createArchive(String path, String archiveName, Manifest manifest) {}

            @Override
            public void saveDirEntry(String path, String archiveName, String entryName) {}

            @Override
            public void copyEntry(String source, String path, String archiveName, String entry) {}

            @Override
            public void saveClassEntry(
                String path, String archiveName, String qualifiedName, String entryName, String content) {
                classes.put(entryName, content);
            }

            @Override
            public void closeArchive(String path, String archiveName) {}
        }

        @Override
        public Integer call() {
            try {
                Map<String, Object> options = new HashMap<>(Map.of(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES,
                    "1", IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1", IFernflowerPreferences.REMOVE_SYNTHETIC,
                    "1", IFernflowerPreferences.LOG_LEVEL, "warn",
                    // Bazel tends to run decompilers in parallel
                    IFernflowerPreferences.THREADS,
                    String.valueOf(Math.min(4, Runtime.getRuntime().availableProcessors())),
                    IFernflowerPreferences.INDENT_STRING, "\t"));
                if (mappings != null) {
                    var mappingsFile = sandboxPath != null ? new File(sandboxPath.toFile(), mappings.toString()) : mappings;
                    options.put(IFabricJavadocProvider.PROPERTY_NAME,
                            new TinyJavadocProvider(mappingsFile));
                }

                try (var saver = new InMemorySaver()) {
                    var vineFlower = new Fernflower(saver, options, new PrintWriterLogger(out));
                    for (var inputFile : inputFiles) {
                        var sourceFile = sandboxPath != null ? new File(sandboxPath.toFile(), inputFile.toString()) : inputFile;
                        vineFlower.addSource(sourceFile);
                    }
                    try {
                        vineFlower.decompileContext();
                    } finally {
                        vineFlower.clearContext();
                    }

                    try (var jos =
                                 new JarOutputStream(Files.newOutputStream(sandboxPath != null ? sandboxPath.resolve(outputFile.toPath()) : outputFile.toPath()))) {
                        saver.classes.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(entry -> {
                            var newEntry = new JarEntry(entry.getKey());
                            setJarEntryTime(newEntry);
                            try {
                                jos.putNextEntry(newEntry);
                                jos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                                jos.closeEntry();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
                return 0;
            } catch (Exception e) {
                e.printStackTrace(out);
                return 1;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new DecompilerWrapper().run(args);
    }
}
