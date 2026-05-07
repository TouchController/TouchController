package top.fifthlight.mergetools.merger.plugin.services;

import top.fifthlight.mergetools.merger.api.AttributeEnvironment;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ServicesPlugin implements Plugin {
    private static final String SERVICES_PREFIX = "META-INF/services/";
    private final Map<String, LinkedHashSet<String>> serviceImplementations = new LinkedHashMap<>();

    @Override
    public int priority() {
        return 300;
    }

    @Override
    public boolean processJarEntry(JarFile file, JarEntry entry, PreprocessEnvironment environment) {
        var name = entry.getName();
        if (!name.startsWith(SERVICES_PREFIX)) {
            return false;
        }

        var serviceName = name.substring(SERVICES_PREFIX.length());
        var implementations = serviceImplementations.computeIfAbsent(serviceName, k -> new LinkedHashSet<>());

        try (var inputStream = new BufferedInputStream(file.getInputStream(entry));
             var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(implementations::add);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read service file: " + name, e);
        }

        return true;
    }

    @Override
    public void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries, AttributeEnvironment environment) {
        for (var entry : serviceImplementations.entrySet()) {
            var serviceName = entry.getKey();
            var mergedContent = String.join("\n", entry.getValue()) + "\n";
            var serviceEntry = new ServicesMergeEntry(serviceName, mergedContent);
            mergeEntries.put(SERVICES_PREFIX + serviceName, serviceEntry);
        }
    }

    private record ServicesMergeEntry(String serviceName, String mergedContent) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws IOException {
            output.write(mergedContent.getBytes(StandardCharsets.UTF_8));
        }
    }
}
