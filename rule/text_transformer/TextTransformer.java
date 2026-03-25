package top.fifthlight.fabazel.texttransformer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.Callable;

@Command(
        name = "TextTransformer",
        mixinStandardHelpOptions = true,
        description = "Transform JSON text resource files to JSON (minified, sorted) or legacy .lang format."
)
public class TextTransformer implements Callable<Integer> {
    @Option(names = {"-i", "--input"}, description = "Input JSON file path", required = true)
    private Path inputFile;

    @Option(names = {"-o", "--output"}, description = "Output file path", required = true)
    private Path outputFile;

    @Option(names = {"-f", "--format"}, description = "Output format: json or lang", required = true)
    private String format;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Integer call() throws Exception {
        if (!format.equals("json") && !format.equals("lang")) {
            System.err.println("Error: Invalid format '" + format + "'. Must be 'json' or 'lang'.");
            return 1;
        }

        Map<String, String> map;
        try (var inputStream = Files.newInputStream(inputFile)) {
            map = OBJECT_MAPPER.readValue(inputStream, new TypeReference<>() {
            });
        }

        var filteredMap = new TreeMap<String, String>();
        for (var entry : map.entrySet()) {
            var value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                filteredMap.put(entry.getKey(), value);
            }
        }

        Files.createDirectories(outputFile.getParent());

        if (format.equals("json")) {
            OBJECT_MAPPER.writeValue(outputFile.toFile(), filteredMap);
        } else {
            var properties = new Properties();
            properties.putAll(filteredMap);
            var writer = new StringWriter();
            properties.store(writer, "PARSE_ESCAPES");
            var content = writer.toString();
            try (var bufferedWriter = Files.newBufferedWriter(outputFile)) {
                var lines = content.lines().toList();
                for (var i = 0; i < lines.size(); i++) {
                    var line = lines.get(i);
                    if (i == 1 && line.startsWith("#")) {
                        continue;
                    }
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
            }
        }

        return 0;
    }

    public static void main(String[] args) {
        var exitCode = new CommandLine(new TextTransformer()).execute(args);
        System.exit(exitCode);
    }
}
