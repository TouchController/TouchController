package top.fifthlight.mergetools.merger;

import org.jspecify.annotations.Nullable;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PluginProvider;
import top.fifthlight.mergetools.merger.impl.PreprocessEnvironmentImpl;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MergeTool implements AutoCloseable {
    private static final long DOS_EPOCH = 315532800000L;

    private static void setZipEntryTime(ZipEntry entry) {
        entry.setCreationTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastAccessTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setLastModifiedTime(FileTime.fromMillis(DOS_EPOCH));
        entry.setTimeLocal(LocalDateTime.ofEpochSecond(DOS_EPOCH / 1000, 0, ZoneOffset.UTC));
    }

    private static final Map<String, PluginProvider> PROVIDERS = ServiceLoader.load(PluginProvider.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toMap(PluginProvider::name, Function.identity()));

    private MergeTool() {
    }

    private final ArrayList<JarFile> jarFiles = new ArrayList<>();
    private Path outputPath;
    private List<Plugin> plugins = List.of();

    public static void process(@Nullable Path sandboxDir, String[] args) throws Exception {
        var plugins = new ArrayList<Plugin>();
        var remaining = new ArrayList<String>();
        var i = 0;
        while (i < args.length) {
            if ("--plugin".equals(args[i]) && i + 1 < args.length) {
                var provider = PROVIDERS.get(args[i + 1]);
                if (provider == null) {
                    throw new IllegalArgumentException("Unknown plugin: " + args[i + 1]);
                }
                plugins.add(provider.create());
                i += 2;
            } else {
                remaining.addAll(Arrays.asList(args).subList(i, args.length));
                break;
            }
        }
        try (var instance = new MergeTool()) {
            instance.setPlugins(plugins);
            var environment = instance.preprocess(sandboxDir, remaining.toArray(new String[0]));
            for (var plugin : instance.plugins) {
                plugin.preSorting(environment.getMergeEntries(), environment.getManifestEntries(), environment);
            }
            var outputEntries = instance.sort(environment.getMergeEntries());
            instance.writeJar(outputEntries, environment);
        }
    }

    private record JarItem(JarFile jarFile, JarEntry entry) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws Exception {
            try (var inputStream = jarFile.getInputStream(entry)) {
                inputStream.transferTo(output);
            }
        }
    }

    private void setPlugins(List<Plugin> plugins) {
        plugins.sort(Comparator.comparingInt(Plugin::priority));
        this.plugins = plugins;
    }

    private List<Map.Entry<String, MergeEntry>> sort(Map<String, MergeEntry> mergeEntries) {
        return mergeEntries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();
    }

    private void writeJar(List<Map.Entry<String, MergeEntry>> entries, PreprocessEnvironmentImpl environment) throws Exception {
        var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        for (var entry : environment.getManifestEntries().entrySet()) {
            manifest.getMainAttributes().putValue(entry.getKey(), entry.getValue());
        }
        try (var outputStream = new ZipOutputStream(Files.newOutputStream(outputPath))) {
            var manifestEntry = new ZipEntry(JarFile.MANIFEST_NAME);
            setZipEntryTime(manifestEntry);
            outputStream.putNextEntry(manifestEntry);
            manifest.write(outputStream);
            outputStream.closeEntry();

            for (var entry : entries) {
                var value = entry.getValue();
                var outputEntry = new ZipEntry(entry.getKey());
                setZipEntryTime(outputEntry);
                outputStream.putNextEntry(outputEntry);
                value.write(outputStream);
                outputStream.closeEntry();
            }
        }
    }

    @Override
    public void close() throws Exception {
        for (var file : jarFiles) {
            file.close();
        }
        for (var plugin : plugins) {
            plugin.close();
        }
    }

    private PreprocessEnvironmentImpl preprocess(@Nullable Path sandboxPath, String[] args) throws Exception {
        var environment = new PreprocessEnvironmentImpl(sandboxPath, args);

        outputPath = sandboxPath != null ? sandboxPath.resolve(Path.of(environment.readNextArg())) : Path.of(environment.readNextArg());

        while (environment.hasNextArg()) {
            var arg = environment.readNextArg();

            var processedArg = false;
            for (var plugin : plugins) {
                processedArg = plugin.processArg(arg, environment);
                if (processedArg) {
                    break;
                }
            }
            if (processedArg) {
                continue;
            }

            var inputPath = environment.resolvePath(Path.of(arg));
            var jarFile = new JarFile(inputPath.toFile());
            jarFiles.add(jarFile);

            var enumerator = jarFile.entries();
            JarEntry jarEntry;
            while (enumerator.hasMoreElements()) {
                jarEntry = enumerator.nextElement();
                if (jarEntry.isDirectory()) {
                    continue;
                }

                var processedEntry = false;
                for (var plugin : plugins) {
                    processedEntry = plugin.processJarEntry(jarFile, jarEntry, environment);
                    if (processedEntry) {
                        break;
                    }
                }
                if (processedEntry) {
                    continue;
                }

                environment.putMergeEntry(jarEntry.getName(), new JarItem(jarFile, jarEntry));
            }
        }
        return environment;
    }
}
