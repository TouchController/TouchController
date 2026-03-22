package net.neoforged.neoform.runtime.tools;

import net.neoforged.srgutils.IMappingFile;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// Code from https://github.com/neoforged/NeoFormRuntime/blob/c1abad7406639837560894b70272625fc42e1bf7/src/main/java/net/neoforged/neoform/runtime/actions/SplitResourcesFromClassesAction.java#L162

/**
 * CLI tool for splitting a JAR file into classes and resources, optionally generating a manifest.
 * <p>
 * Usage:
 * <pre>
 * java -cp ... SplitResourcesFromClasses --input input.jar --classes-output classes.jar --resources-output resources.jar
 *   [--other-dist-jar other.jar --mappings mappings.txt --dist-id client --other-dist-id server]
 *   [--deny-pattern "pattern1" --deny-pattern "pattern2"]
 * </pre>
 */
public class SplitResourcesFromClasses {
    /**
     * Use a fixed timestamp for the manifest entry.
     */
    private static final LocalDateTime MANIFEST_TIME = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);

    /**
     * Patterns for filenames that should not be written to either output jar.
     */
    private final List<Pattern> denyListPatterns = new ArrayList<>();

    /**
     * Settings for generating the distribution manifest.
     */
    private GenerateDistManifestSettings generateDistManifestSettings;

    /**
     * Main entry point for the CLI tool.
     */
    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("--help")) {
            printUsage();
            return;
        }

        try {
            var splitter = new SplitResourcesFromClasses();
            var arguments = parseArguments(args);

            splitter.process(
                    arguments.inputJar,
                    arguments.classesOutput,
                    arguments.resourcesOutput,
                    arguments.otherDistJar,
                    arguments.mappingsFile,
                    arguments.distId,
                    arguments.otherDistId,
                    arguments.denyPatterns
            );
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Process the JAR file splitting.
     */
    public void process(Path inputJar, Path classesOutput, Path resourcesOutput,
                        Path otherDistJar, Path mappingsFile,
                        String distId, String otherDistId,
                        List<String> denyPatterns) throws IOException {

        // Set up deny patterns
        if (denyPatterns != null) {
            for (var pattern : denyPatterns) {
                denyListPatterns.add(Pattern.compile(pattern));
            }
        }

        // Set up manifest generation if needed
        if (distId != null && otherDistId != null && otherDistJar != null && mappingsFile != null) {
            generateDistManifestSettings = new GenerateDistManifestSettings(distId, otherDistId);
        }

        Predicate<String> denyPredicate = s -> false;
        if (!denyListPatterns.isEmpty()) {
            // Build a combined regular expression to speed things up
            denyPredicate = Pattern
                    .compile(denyListPatterns.stream().map(Pattern::pattern).collect(Collectors.joining("|")))
                    .asMatchPredicate();
        }

        try (var jar = new ZipFile(inputJar.toFile());
             var classesFileOut = new BufferedOutputStream(Files.newOutputStream(classesOutput));
             var resourcesFileOut = new BufferedOutputStream(Files.newOutputStream(resourcesOutput));
             var classesJarOut = new JarOutputStream(classesFileOut);
             var resourcesJarOut = new JarOutputStream(resourcesFileOut)
        ) {
            if (generateDistManifestSettings != null) {
                generateDistSourceManifest(
                        generateDistManifestSettings.distId(),
                        jar,
                        generateDistManifestSettings.otherDistId(),
                        otherDistJar,
                        mappingsFile,
                        resourcesJarOut
                );
            }

            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue; // For simplicity, we ignore directories completely
                }

                // If this task generates its own manifest, ignore any manifests found in the input jar
                if (generateDistManifestSettings != null && entry.getName().equals(JarFile.MANIFEST_NAME)) {
                    continue;
                }

                var filename = entry.getName();

                // Skip anything that looks like a signature file
                if (denyPredicate.test(filename)) {
                    continue;
                }

                var destinationStream = filename.endsWith(".class") ? classesJarOut : resourcesJarOut;

                destinationStream.putNextEntry(entry);
                try (var is = jar.getInputStream(entry)) {
                    is.transferTo(destinationStream);
                }
                destinationStream.closeEntry();
            }
        }
    }

    private static void generateDistSourceManifest(String distId,
                                                   ZipFile jar,
                                                   String otherDistId,
                                                   Path otherDistJarPath,
                                                   Path mappingsPath,
                                                   JarOutputStream resourcesJarOut) throws IOException {
        var mappings = mappingsPath != null ? IMappingFile.load(mappingsPath.toFile()) : null;

        // Use the time-stamp of either of the two input files (whichever is newer)
        var ourFiles = getFileIndex(jar);
        ourFiles.remove(JarFile.MANIFEST_NAME);
        Set<String> theirFiles;
        try (var otherDistJar = new ZipFile(otherDistJarPath.toFile())) {
            theirFiles = getFileIndex(otherDistJar);
        }
        theirFiles.remove(JarFile.MANIFEST_NAME);

        var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Minecraft-Dists", distId + " " + otherDistId);

        addSourceDistEntries(ourFiles, theirFiles, distId, mappings, manifest);
        addSourceDistEntries(theirFiles, ourFiles, otherDistId, mappings, manifest);

        var manifestEntry = new ZipEntry(JarFile.MANIFEST_NAME);
        manifestEntry.setTimeLocal(MANIFEST_TIME);
        resourcesJarOut.putNextEntry(manifestEntry);
        manifest.write(resourcesJarOut);
        resourcesJarOut.closeEntry();
    }

    private static void addSourceDistEntries(Set<String> distFiles,
                                             Set<String> otherDistFiles,
                                             String dist,
                                             IMappingFile mappings,
                                             Manifest manifest) {
        for (var file : distFiles) {
            if (!otherDistFiles.contains(file)) {
                var fileAttr = new Attributes(1);
                fileAttr.putValue("Minecraft-Dist", dist);

                if (mappings != null && file.endsWith(".class")) {
                    file = mappings.remapClass(file.substring(0, file.length() - ".class".length())) + ".class";
                }
                manifest.getEntries().put(file, fileAttr);
            }
        }
    }

    private static Set<String> getFileIndex(ZipFile zipFile) {
        var result = new HashSet<String>(zipFile.size());

        var entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            var entry = entries.nextElement();
            if (!entry.isDirectory()) {
                result.add(entry.getName());
            }
        }

        return result;
    }

    private static void printUsage() {
        System.out.println("SplitResourcesFromClasses");
        System.out.println("Usage:");
        System.out.println("  --input <file>             Input JAR file to split (required)");
        System.out.println("  --classes-output <file>    Output JAR for classes (required)");
        System.out.println("  --resources-output <file>  Output JAR for resources (required)");
        System.out.println("  --other-dist-jar <file>    Other distribution JAR (for manifest generation)");
        System.out.println("  --mappings <file>          Mappings file (for manifest generation)");
        System.out.println("  --dist-id <id>             Distribution ID (e.g., 'client')");
        System.out.println("  --other-dist-id <id>       Other distribution ID (e.g., 'server')");
        System.out.println("  --deny-pattern <pattern>   Regex pattern for files to exclude (can be repeated)");
        System.out.println("  --help                     Show this help message");
    }

    private static Arguments parseArguments(String[] args) {
        var arguments = new Arguments();
        List<String> denyPatterns = new ArrayList<>();

        for (var i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--input":
                    arguments.inputJar = Path.of(args[++i]);
                    break;
                case "--classes-output":
                    arguments.classesOutput = Path.of(args[++i]);
                    break;
                case "--resources-output":
                    arguments.resourcesOutput = Path.of(args[++i]);
                    break;
                case "--other-dist-jar":
                    arguments.otherDistJar = Path.of(args[++i]);
                    break;
                case "--mappings":
                    arguments.mappingsFile = Path.of(args[++i]);
                    break;
                case "--dist-id":
                    arguments.distId = args[++i];
                    break;
                case "--other-dist-id":
                    arguments.otherDistId = args[++i];
                    break;
                case "--deny-pattern":
                    denyPatterns.add(args[++i]);
                    break;
            }
        }

        arguments.denyPatterns = denyPatterns;

        // Validate required arguments
        if (arguments.inputJar == null || arguments.classesOutput == null || arguments.resourcesOutput == null) {
            throw new IllegalArgumentException("Missing required arguments");
        }

        // Validate manifest generation arguments (all or none)
        var hasSomeManifestArgs = arguments.otherDistJar != null || arguments.mappingsFile != null ||
                arguments.distId != null || arguments.otherDistId != null;
        var hasAllManifestArgs = arguments.otherDistJar != null && arguments.mappingsFile != null &&
                arguments.distId != null && arguments.otherDistId != null;

        if (hasSomeManifestArgs && !hasAllManifestArgs) {
            throw new IllegalArgumentException("For manifest generation, all of --other-dist-jar, --mappings, --dist-id, and --other-dist-id must be provided");
        }

        return arguments;
    }

    private static class Arguments {
        Path inputJar;
        Path classesOutput;
        Path resourcesOutput;
        Path otherDistJar;
        Path mappingsFile;
        String distId;
        String otherDistId;
        List<String> denyPatterns;
    }

    private record GenerateDistManifestSettings(
            String distId,
            String otherDistId
    ) {
    }
}
