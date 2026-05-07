package top.fifthlight.mergetools.merger.plugin.resource;

import org.jetbrains.annotations.Nullable;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourcePlugin implements Plugin {
    @Override
    public int priority() {
        return 100;
    }

    @Nullable
    private String currentStrip = null;
    private String currentPrefix = "";
    private final Map<String, String> renames = new LinkedHashMap<>();

    private record ResourceFileEntry(Path resourceFile) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws IOException {
            try (var inputStream = Files.newInputStream(resourceFile)) {
                inputStream.transferTo(output);
            }
        }
    }

    private String computeEntryPath(String filePath) {
        var entryPath = filePath.replace('\\', '/');

        if (currentStrip != null) {
            if (currentStrip.isEmpty()) {
                var lastSlash = entryPath.lastIndexOf('/');
                entryPath = lastSlash >= 0 ? entryPath.substring(lastSlash + 1) : entryPath;
            } else {
                var index = entryPath.indexOf(currentStrip);
                if (index == -1) {
                    throw new IllegalArgumentException(
                            "Invalid resource path: " + entryPath + ", not matching strip: " + currentStrip);
                }
                entryPath = entryPath.substring(index + currentStrip.length());
                if (entryPath.startsWith("/")) {
                    entryPath = entryPath.substring(1);
                }
            }
        }

        var lastSlash = entryPath.lastIndexOf('/');
        var basename = lastSlash >= 0 ? entryPath.substring(lastSlash + 1) : entryPath;
        var dirname = lastSlash >= 0 ? entryPath.substring(0, lastSlash) : "";
        var newBasename = renames.getOrDefault(basename, basename);
        entryPath = dirname.isEmpty() ? newBasename : dirname + "/" + newBasename;

        if (!currentPrefix.isEmpty()) {
            entryPath = currentPrefix + "/" + entryPath;
        }

        return entryPath;
    }

    @Override
    public boolean processArg(String arg, PreprocessEnvironment environment) {
        return switch (arg) {
            case "--resource-strip" -> {
                var strip = environment.readNextArg();
                currentStrip = strip.equals(".") ? "" : strip;
                yield true;
            }

            case "--resource-prefix" -> {
                currentPrefix = environment.readNextArg();
                yield true;
            }

            case "--resource-rename" -> {
                var from = environment.readNextArg();
                var to = environment.readNextArg();
                renames.put(from, to);
                yield true;
            }

            case "--resource" -> {
                var filePath = environment.readNextArg();
                var entryPath = computeEntryPath(filePath);
                environment.putMergeEntry(entryPath, new ResourceFileEntry(environment.resolvePath(Path.of(filePath))));
                yield true;
            }

            case "--resource-path" -> {
                var entryPath = environment.readNextArg();
                var filePath = environment.readNextArg();
                environment.putMergeEntry(entryPath, new ResourceFileEntry(environment.resolvePath(Path.of(filePath))));
                yield true;
            }

            default -> false;
        };
    }

    @Override
    public boolean processJarEntry(JarFile file, JarEntry entry, PreprocessEnvironment environment) {
        return false;
    }
}
