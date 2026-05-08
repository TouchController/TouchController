package top.fifthlight.mergetools.merger.plugin.unifiedjar;

import com.fasterxml.jackson.databind.ObjectMapper;
import top.fifthlight.mergetools.merger.api.AttributeEnvironment;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;
import top.fifthlight.mergetools.merger.plugin.jarinjar.JarInJarPlugin;
import top.fifthlight.multijar.common.MultiJarManifest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class UnifiedJarManifestPlugin implements Plugin {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LinkedHashMap<String, HashSet<String>> neoforgeGroups = new LinkedHashMap<>();
    private final LinkedHashMap<String, HashSet<String>> forgeGroups = new LinkedHashMap<>();

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
                neoforgeGroups.computeIfAbsent(modid, k -> new HashSet<>()).add(mcGroup);
                yield true;
            }
            case "--unified-forge" -> {
                var modid = environment.readNextArg();
                var mcGroup = environment.readNextArg();
                forgeGroups.computeIfAbsent(modid, k -> new HashSet<>()).add(mcGroup);
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries, AttributeEnvironment environment) {
        var context = environment.getAttribute(JarInJarPlugin.JIJ_CONTEXT);

        if (!neoforgeGroups.isEmpty()) {
            writeManifest(mergeEntries, MultiJarManifest.NEOFORGE_MANIFEST_PATH, neoforgeGroups, context);
        }
        if (!forgeGroups.isEmpty()) {
            writeManifest(mergeEntries, MultiJarManifest.FORGE_MANIFEST_PATH, forgeGroups, context);
        }
    }

    private void writeManifest(Map<String, MergeEntry> mergeEntries, String manifestPath,
                               LinkedHashMap<String, HashSet<String>> groups, JarInJarPlugin.JiJContext context) {
        var groupMap = new LinkedHashMap<String, List<String>>();
        for (var entry : groups.entrySet()) {
            var modid = entry.getKey();
            for (var mcGroup : entry.getValue()) {
                groupMap.computeIfAbsent(mcGroup, k -> new ArrayList<>()).add(modid);
            }
        }

        var json = MAPPER.createObjectNode();
        for (var group : groupMap.entrySet()) {
            var jarsArray = MAPPER.createArrayNode();
            for (var modid : group.getValue()) {
                var jarPath = context != null && context.entries().containsKey(modid)
                        ? context.entries().get(modid).entryPath()
                        : JarInJarPlugin.JARS_BASE_PATH + modid + ".jar";
                jarsArray.add(jarPath);
            }
            json.set(group.getKey(), jarsArray);
        }

        try {
            mergeEntries.put(manifestPath, new BytesMergeEntry(MAPPER.writeValueAsBytes(json)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record BytesMergeEntry(byte[] content) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws IOException {
            output.write(content);
        }
    }
}
