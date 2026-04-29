package top.fifthlight.armorstand.updatelogextractor;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.fifthlight.bazel.worker.api.Worker;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class UpdateLogExtractor extends Worker {
    public static void main(String[] args) throws Exception {
        new UpdateLogExtractor().run(args);
    }

    @Override
    protected int handleRequest(@NonNull PrintWriter out, @Nullable Path sandboxDir, @NonNull String... args) throws Exception {
        if (args.length < 3) {
            out.println("Usage: UpdateLogExtractor <version name> <output file> <input file>...");
            return 1;
        }
        var versionName = args[0];
        var outputPath = sandboxDir != null ? sandboxDir.resolve(Path.of(args[1])) : Path.of(args[1]);

        try (var writer = Files.newBufferedWriter(outputPath)) {
            for (var i = 2; i < args.length; i++) {
                var updateLogPath = sandboxDir != null ? sandboxDir.resolve(Path.of(args[i])) : Path.of(args[i]);

                try (var reader = Files.newBufferedReader(updateLogPath)) {
                    var logContentBuilder = new StringBuilder();
                    var foundVersion = false;
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().startsWith("## " + versionName)) {
                            foundVersion = true;
                            continue;
                        }

                        if (foundVersion) {
                            if (line.trim().startsWith("## ")) {
                                break;
                            }
                            logContentBuilder.append(line).append(System.lineSeparator());
                        }
                    }

                    var logText = logContentBuilder.toString().trim();
                    if (!logText.isEmpty()) {
                        if (i > 2) {
                            writer.newLine();
                            writer.write("---");
                            writer.newLine();
                            writer.newLine();
                        }
                        writer.write(logText);
                        writer.newLine();
                    }
                }
            }
        }
        return 0;
    }
}
