package top.fifthlight.fabazel.jarcreator;

import org.jspecify.annotations.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class JarCreator {
    private static final long DOS_EPOCH = 315532800000L;

    private static void setJarEntryTime(JarEntry entry) {
        entry.setCreationTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastAccessTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastModifiedTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setTimeLocal(LocalDateTime.ofEpochSecond(DOS_EPOCH / 1000, 0, ZoneOffset.UTC));
    }

    private final @Nullable Path sandboxDir;

    public JarCreator(@Nullable Path sandboxDir) {
        this.sandboxDir = sandboxDir;
    }

    public static void run(PrintWriter out, @Nullable Path sandboxDir, String... args) throws Exception {
        if (args.length < 1) {
            out.println("Usage: JarCreator <output-jar> [options]");
            out.println("Options:");
            out.println("  --entry <jar-path> <file-path>  Add file path in JAR");
            out.println("  --output <output-jar> Output JAR path");
        }

        var creator = new JarCreator(sandboxDir);
        creator.processArgs(args);
        creator.createJar();
    }

    private final Map<String, Path> entries = new HashMap<>();
    private Path outputPath;

    private void processArgs(String[] args) {
        for (var i = 0; i < args.length; i++) {
            var arg = args[i];

            switch (arg) {
                case "--entry" -> {
                    if (i + 2 >= args.length) {
                        throw new IllegalArgumentException("Missing arguments for --entry");
                    }
                    var entryPath = args[++i];
                    if (entries.containsKey(entryPath)) {
                        throw new IllegalArgumentException("Duplicate entry: " + entryPath);
                    }
                    entries.put(entryPath, sandboxDir != null ? sandboxDir.resolve(Path.of(args[++i])) : Path.of(args[++i]));
                }

                case "--output" -> {
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Missing arguments for --output");
                    }
                    outputPath = sandboxDir != null ? sandboxDir.resolve(Path.of(args[++i])) : Path.of(args[++i]);
                }

                default -> throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
    }

    private record JarEntryData(String entry, Path filePath) {
    }

    private void createJar() throws IOException {
        var entries = this.entries.entrySet().stream()
                .map(entry -> new JarEntryData(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(JarEntryData::entry))
                .toList();
        try (var jarOut = new JarOutputStream(new FileOutputStream(outputPath.toFile()))) {
            for (var data : entries) {
                var jarEntry = new JarEntry(data.entry);
                setJarEntryTime(jarEntry);
                jarOut.putNextEntry(jarEntry);
                try (var inputStream = Files.newInputStream(data.filePath)) {
                    inputStream.transferTo(jarOut);
                }
                jarOut.closeEntry();
            }
        }
    }
}
