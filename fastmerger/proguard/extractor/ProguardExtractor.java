package top.fifthlight.fastmerger.proguard.extractor;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.fifthlight.bazel.worker.api.Worker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ProguardExtractor extends Worker {
    @Override
    protected int handleRequest(@NonNull PrintWriter out, @Nullable Path sandboxDir, String... args) throws IOException {
        if (args.length != 2) {
            out.println("Usage: <jar-path> <output-pro>");
            return 1;
        }
        var jarPath = sandboxDir != null ? sandboxDir.resolve(Path.of(args[0])) : Path.of(args[0]);
        var outputPath = sandboxDir != null ? sandboxDir.resolve(Path.of(args[1])) : Path.of(args[1]);
        try (var jarInputStream = new JarInputStream(Files.newInputStream(jarPath));
             var outputStream = new PrintWriter(Files.newBufferedWriter(outputPath))) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                var isAndroidAarRule = entry.getName().equals("proguard.txt");
                var isProguardFile = entry.getName().startsWith("META-INF/proguard/") && entry.getName().endsWith(".pro");
                if (!isAndroidAarRule && !isProguardFile) {
                    continue;
                }
                var reader = new InputStreamReader(jarInputStream, StandardCharsets.UTF_8);
                reader.transferTo(outputStream);
                outputStream.println();
                jarInputStream.closeEntry();
            }
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        new ProguardExtractor().run(args);
    }
}
