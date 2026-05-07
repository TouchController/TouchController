package top.fifthlight.mergetools.merger.plugin.manifest;

import top.fifthlight.mergetools.merger.api.AttributeEnvironment;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;

import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ManifestPlugin implements Plugin {
    @Override
    public int priority() {
        return 200;
    }

    private enum ManifestMode {
        IGNORE_ALL,
        USE_FIRST_BY_ALPHABET,
        USE_LAST_BY_ALPHABET,
    }

    private ManifestMode manifestMode = ManifestMode.IGNORE_ALL;

    private record ManifestEntry(String key, String value) implements Comparable<ManifestEntry> {
        @Override
        public int compareTo(ManifestEntry o) {
            return this.value.compareTo(o.value);
        }
    }

    private final List<ManifestEntry> jarManifestEntries = new ArrayList<>();
    private final Map<String, String> argManifestEntries = new HashMap<>();

    @Override
    public boolean processArg(String arg, PreprocessEnvironment environment) {
        return switch (arg) {
            case "--manifest" -> {
                var key = environment.readNextArg();
                var value = environment.readNextArg();
                argManifestEntries.put(key, value);
                yield true;
            }

            case "--manifest-mode" -> {
                var mode = environment.readNextArg();
                manifestMode = switch (mode) {
                    case "ignore-all" -> ManifestMode.IGNORE_ALL;
                    case "use-first-by-alphabet" -> ManifestMode.USE_FIRST_BY_ALPHABET;
                    case "use-last-by-alphabet" -> ManifestMode.USE_LAST_BY_ALPHABET;
                    default -> throw new IllegalArgumentException("Unknown manifest mode: " + mode);
                };
                yield true;
            }

            default -> false;
        };
    }

    @Override
    public boolean processJarEntry(JarFile file, JarEntry entry, PreprocessEnvironment environment) throws IOException {
        if (!entry.getName().equals("META-INF/MANIFEST.MF")) {
            return false;
        }
        var manifest = new Manifest(file.getInputStream(entry));
        for (var manifestEntry : manifest.getMainAttributes().entrySet()) {
            jarManifestEntries.add(new ManifestEntry(manifestEntry.getKey().toString(), manifestEntry.getValue().toString()));
        }
        return true;
    }

    @Override
    public void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries, AttributeEnvironment environment) {
        switch (manifestMode) {
            case IGNORE_ALL -> {
            }
            // Reverse, so that the last one is first
            case USE_FIRST_BY_ALPHABET -> jarManifestEntries.stream()
                    .sorted(Comparator.reverseOrder())
                    .forEachOrdered(entry -> manifestEntries.put(entry.key, entry.value));
            // Don't reverse, so that the last one is last
            case USE_LAST_BY_ALPHABET -> jarManifestEntries
                    .forEach(entry -> manifestEntries.put(entry.key, entry.value));
        }
        manifestEntries.putAll(argManifestEntries);
    }
}
