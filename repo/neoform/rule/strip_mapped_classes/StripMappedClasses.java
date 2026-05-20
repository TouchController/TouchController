package net.neoforged.neoform.runtime.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * CLI tool for filtering JAR entries based on a TSrg mapping file (whitelist/blacklist mode).
 * Equivalent to ForgeGradle's {@code StripJarFunction}.
 * <p>
 * Usage:
 * <pre>
 * java ... StripMappedClasses --input input.jar --output output.jar --mappings joined.tsrg [--mode whitelist|blacklist]
 * </pre>
 * <p>
 * The TSrg format: non-indented lines are class entries. The first space-separated token is the
 * obfuscated class name. The tool appends ".class" to build the filter set.
 */
public class StripMappedClasses {
    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("--help")) {
            System.out.println("StripMappedClasses");
            System.out.println("Usage:");
            System.out.println("  --input <file>     Input JAR file (required)");
            System.out.println("  --output <file>    Output JAR file (required)");
            System.out.println("  --mappings <file>  TSrg mappings file (required)");
            System.out.println("  --mode <mode>      Filter mode: whitelist or blacklist (default: whitelist)");
            System.out.println("  --help             Show this help message");
            return;
        }

        try {
            var arguments = parseArguments(args);
            process(arguments);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void process(Arguments arguments) throws IOException {
        // Build filter set from TSrg mappings
        Set<String> filter = new HashSet<>();
        try (var reader = Files.newBufferedReader(arguments.mappingsFile)) {
            reader.lines()
                    .filter(l -> !l.isEmpty() && !l.startsWith("\t"))
                    .map(l -> l.split(" ")[0] + ".class")
                    .forEach(filter::add);
        }
        System.err.println("Filter entries: " + filter.size());

        var whitelist = !"blacklist".equalsIgnoreCase(arguments.mode);
        var kept = 0;
        var skipped = 0;

        try (var zin = new JarInputStream(new BufferedInputStream(Files.newInputStream(arguments.inputFile)));
             var zout = new JarOutputStream(new BufferedOutputStream(Files.newOutputStream(arguments.outputFile)))) {

            JarEntry entry;
            while ((entry = zin.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                var inFilter = filter.contains(entry.getName());
                if (inFilter == whitelist) {
                    zout.putNextEntry(entry);
                    zin.transferTo(zout);
                    zout.closeEntry();
                    kept++;
                } else {
                    skipped++;
                }
            }
        }

        System.err.println("Kept: " + kept + ", Skipped: " + skipped);
    }

    private static Arguments parseArguments(String[] args) {
        var arguments = new Arguments();

        for (var i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--input":
                    arguments.inputFile = Path.of(args[++i]);
                    break;
                case "--output":
                    arguments.outputFile = Path.of(args[++i]);
                    break;
                case "--mappings":
                    arguments.mappingsFile = Path.of(args[++i]);
                    break;
                case "--mode":
                    arguments.mode = args[++i];
                    break;
            }
        }

        if (arguments.inputFile == null || arguments.outputFile == null || arguments.mappingsFile == null) {
            throw new IllegalArgumentException("Missing required arguments: --input, --output, and --mappings are required");
        }

        return arguments;
    }

    private static class Arguments {
        Path inputFile;
        Path outputFile;
        Path mappingsFile;
        String mode = "whitelist";
    }
}
