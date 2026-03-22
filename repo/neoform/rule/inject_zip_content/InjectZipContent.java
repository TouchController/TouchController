package net.neoforged.neoform.runtime.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Injects additional content from {@linkplain InjectSource configurable sources} into a Zip (or
 * Jar) file. <br> Standalone CLI tool version.
 */
public class InjectZipContent {
    /**
     * Base interface for injection sources
     */
    private interface InjectSource {
        void copyTo(ZipOutputStream zos) throws IOException;

        byte[] tryReadFile(String filename) throws IOException;
    }

    /**
     * Injection source from a directory
     */
    private record DirectoryInjectSource(Path directory) implements InjectSource {
        @Override
        public void copyTo(ZipOutputStream zos) throws IOException {
            if (!Files.exists(directory)) {
                return;
            }

            Files.walk(directory, FileVisitOption.FOLLOW_LINKS)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        var relativePath = directory.relativize(path).toString().replace('\\', '/');
                        zos.putNextEntry(new ZipEntry(relativePath));
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }

        @Override
        public byte[] tryReadFile(String filename) throws IOException {
            var filePath = directory.resolve(filename);
            if (Files.exists(filePath)) {
                return Files.readAllBytes(filePath);
            }
            return null;
        }
    }

    /**
     * Injection source from a ZIP file
     */
    private record ZipInjectSource(Path zipFile) implements InjectSource {
        @Override
        public void copyTo(ZipOutputStream zos) throws IOException {
            if (!Files.exists(zipFile)) {
                return;
            }

            try (var zis = new ZipInputStream(Files.newInputStream(zipFile))) {
                ZipEntry entry;
                var buffer = new byte[8192];
                int length;

                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }

                    try {
                        zos.putNextEntry(entry);
                        while ((length = zis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                        zos.closeEntry();
                        zis.closeEntry();
                    } catch (ZipException e) {
                        if (!e.getMessage().startsWith("duplicate entry:")) {
                            throw e;
                        }
                        System.err.println("Cannot inject duplicate file " + entry.getName());
                    }
                }
            }
        }

        @Override
        public byte[] tryReadFile(String filename) throws IOException {
            if (!Files.exists(zipFile)) {
                return null;
            }

            try (var zis = new ZipInputStream(Files.newInputStream(zipFile))) {
                ZipEntry entry;

                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals(filename)) {
                        return zis.readAllBytes();
                    }
                    zis.closeEntry();
                }
            }
            return null;
        }
    }

    private List<InjectSource> injectedSources;

    private InjectZipContent(List<InjectSource> injectedSources) {
        this.injectedSources = new ArrayList<>(injectedSources);
    }

    private List<InjectSource> getInjectedSources() {
        return injectedSources;
    }

    private void setInjectedSources(List<InjectSource> injectedSources) {
        this.injectedSources = new ArrayList<>(Objects.requireNonNull(injectedSources));
    }

    private void execute(Path inputZipFile, Path outputZipFile) throws IOException {
        var packageInfoTemplateContent = findPackageInfoTemplate(injectedSources);

        try (var fileOut = Files.newOutputStream(outputZipFile);
            var zos = new ZipOutputStream(fileOut)) {
            copyInputZipContent(inputZipFile, zos, packageInfoTemplateContent);

            // Copy over the injection sources
            for (var injectedSource : injectedSources) {
                injectedSource.copyTo(zos);
            }
        }
    }

    /*
     * We support automatically adding package-info.java files to the source jar based on a
     * template-file found in any one of the inject directories.
     */
    private String findPackageInfoTemplate(List<InjectSource> injectedSources) throws IOException {
        // Try to find a package-info-template.java
        for (var injectedSource : injectedSources) {
            var content = injectedSource.tryReadFile("package-info-template.java");
            if (content != null) {
                return new String(content, StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /*
     * Copies the original ZIP content while applying the optional package-info.java transform.
     */
    private void copyInputZipContent(Path inputZipFile, ZipOutputStream zos,
        String packageInfoTemplateContent) throws IOException {
        Set<String> visited = new HashSet<>();
        try (var zis = new ZipInputStream(Files.newInputStream(inputZipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                zos.putNextEntry(entry);
                zis.transferTo(zos);
                zos.closeEntry();

                if (packageInfoTemplateContent != null) {
                    var pkg = entry.isDirectory() && !entry.getName().endsWith("/")
                        ? entry.getName()
                        : entry.getName().indexOf('/') == -1
                        ? ""
                        : entry.getName().substring(0, entry.getName().lastIndexOf('/'));
                    if (visited.add(pkg)) {
                        if (!pkg.startsWith("net/minecraft/") && !pkg.startsWith("com/mojang/")) {
                            continue;
                        }
                        zos.putNextEntry(new ZipEntry(pkg + "/package-info.java"));
                        zos.write(packageInfoTemplateContent
                                .replace("{PACKAGE}", pkg.replaceAll("/", "."))
                                .getBytes(StandardCharsets.UTF_8));
                        zos.closeEntry();
                    }
                }
            }
        }
    }

    /**
     * CLI entry point for standalone execution
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java InjectZipContentAction <input.zip> <output.zip> "
                               + "<injectSource1> [<injectSource2> ...]");
            System.exit(1);
        }

        try {
            var inputFile = Path.of(args[0]);
            var outputFile = Path.of(args[1]);

            if (!Files.exists(inputFile)) {
                System.err.println("Input file does not exist: " + inputFile);
                System.exit(1);
            }

            // Create inject sources from the remaining arguments
            List<InjectSource> injectSources = new ArrayList<>();
            for (var i = 2; i < args.length; i++) {
                var sourcePath = Path.of(args[i]);
                if (Files.isDirectory(sourcePath)) {
                    injectSources.add(new DirectoryInjectSource(sourcePath));
                } else if (Files.isRegularFile(sourcePath)) {
                    injectSources.add(new ZipInjectSource(sourcePath));
                } else {
                    System.err.println(
                        "Invalid inject source (not a file or directory): " + sourcePath);
                    System.exit(1);
                }
            }

            // Execute the injection
            var action = new InjectZipContent(injectSources);
            action.execute(inputFile, outputFile);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
