package top.fifthlight.mergetools.merger.plugin.unifiedjar;

import top.fifthlight.mergetools.merger.api.AttributeEnvironment;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;
import top.fifthlight.mergetools.merger.plugin.jarinjar.JarInJarPlugin;
import top.fifthlight.multijar.common.MultiJarManifest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UnifiedJarManifestPlugin implements Plugin {
    private final LinkedHashMap<String, HashSet<String>> neoForgeGroups = new LinkedHashMap<>();
    private final LinkedHashMap<String, HashSet<String>> forgeGroups = new LinkedHashMap<>();
    private final HashMap<String, String> mergeDeps = new HashMap<>();

    @Override
    public int priority() {
        return 403;
    }

    @Override
    public boolean processArg(String arg, PreprocessEnvironment environment) {
        return switch (arg) {
            case "--unified-neoforge" -> {
                var modid = environment.readNextArg();
                var mcGroup = environment.readNextArg();
                neoForgeGroups.computeIfAbsent(modid, k -> new HashSet<>()).add(mcGroup);
                yield true;
            }
            case "--unified-forge" -> {
                var modid = environment.readNextArg();
                var mcGroup = environment.readNextArg();
                forgeGroups.computeIfAbsent(modid, k -> new HashSet<>()).add(mcGroup);
                yield true;
            }
            case "--merge-deps" -> {
                mergeDeps.put(environment.readNextArg(), environment.readNextArg());
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries, AttributeEnvironment environment) {
        var context = environment.getAttribute(JarInJarPlugin.JIJ_CONTEXT);

        if (!neoForgeGroups.isEmpty()) {
            writeManifest(mergeEntries, MultiJarManifest.NEOFORGE_MANIFEST_PATH, neoForgeGroups, context);
        }
        if (!forgeGroups.isEmpty()) {
            writeManifest(mergeEntries, MultiJarManifest.FORGE_MANIFEST_PATH, forgeGroups, context);
        }
    }

    private void writeManifest(Map<String, MergeEntry> mergeEntries, String manifestPath,
                               LinkedHashMap<String, HashSet<String>> groups, JarInJarPlugin.JiJContext context) {
        var mergeTargets = new HashMap<String, List<String>>();
        for (var entry : mergeDeps.entrySet()) {
            mergeTargets.computeIfAbsent(entry.getValue(), key -> new ArrayList<>()).add(entry.getKey());
        }

        var groupMap = new LinkedHashMap<String, List<String>>();
        for (var entry : groups.entrySet()) {
            var modid = entry.getKey();
            for (var mcGroup : entry.getValue()) {
                groupMap.computeIfAbsent(mcGroup, key -> new ArrayList<>()).add(modid);
            }
        }

        var builder = MultiJarManifest.builder();
        for (var group : groupMap.entrySet()) {
            var mcGroup = group.getKey();
            var modids = group.getValue();
            var handled = new HashSet<String>();
            for (var modid : modids) {
                if (!handled.add(modid)) {
                    continue;
                }

                var sources = mergeTargets.get(modid);
                if (sources != null) {
                    var merged = new ArrayList<String>();
                    merged.add(getJarPath(modid, context));
                    for (var source : sources) {
                        if (modids.contains(source) && !handled.contains(source)) {
                            handled.add(source);
                            merged.add(getJarPath(source, context));
                        }
                    }
                    if (merged.size() > 1) {
                        builder.addEntry(mcGroup, new MultiJarManifest.JarItem(merged));
                        continue;
                    }
                }
                builder.addEntry(mcGroup, new MultiJarManifest.JarItem(getJarPath(modid, context)));
            }
        }

        mergeEntries.put(manifestPath, new ManifestEntry(builder.build()));
    }

    private String getJarPath(String modid, JarInJarPlugin.JiJContext context) {
        return context != null && context.entries().containsKey(modid)
                ? context.entries().get(modid).entryPath()
                : JarInJarPlugin.JARS_BASE_PATH + modid + ".jar";
    }

    private record ManifestEntry(MultiJarManifest manifest) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws IOException {
            output.write(manifest.toJson().getBytes(StandardCharsets.UTF_8));
        }
    }
}
