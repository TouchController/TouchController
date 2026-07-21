package top.fifthlight.mergetools.merger.plugin.unifiedjar;

import top.fifthlight.mergetools.merger.api.AttributeEnvironment;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;
import top.fifthlight.mergetools.merger.plugin.jarinjar.JarInJarPlugin;
import top.fifthlight.multijar.common.MultiJarCondition;
import top.fifthlight.multijar.common.MultiJarManifest;
import top.fifthlight.multijar.common.MultiJarRule;
import top.fifthlight.multijar.common.RawMultiJarManifest;
import top.fifthlight.multijar.common.RawVersionRange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UnifiedJarManifestPlugin implements Plugin {
    private final LinkedHashMap<String, String> neoForgeRules = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> forgeRules = new LinkedHashMap<>();
    private final HashMap<String, String> mergeDeps = new HashMap<>();

    @Override
    public int priority() {
        return 403;
    }

    @Override
    public boolean processArg(String arg, PreprocessEnvironment environment) {
        return switch (arg) {
            case "--unified-neoforge-rule" -> {
                var modid = environment.readNextArg();
                var conditionSpec = environment.readNextArg();
                neoForgeRules.put(modid, conditionSpec);
                yield true;
            }
            case "--unified-forge-rule" -> {
                var modid = environment.readNextArg();
                var conditionSpec = environment.readNextArg();
                forgeRules.put(modid, conditionSpec);
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

        if (!neoForgeRules.isEmpty()) {
            writeManifest(mergeEntries, MultiJarManifest.NEOFORGE_MANIFEST_PATH, neoForgeRules, context);
        }
        if (!forgeRules.isEmpty()) {
            writeManifest(mergeEntries, MultiJarManifest.FORGE_MANIFEST_PATH, forgeRules, context);
        }
    }

    private void writeManifest(
            Map<String, MergeEntry> mergeEntries,
            String manifestPath,
            LinkedHashMap<String, String> rules,
            JarInJarPlugin.JiJContext context
    ) {
        var mergeTargets = new HashMap<String, List<String>>();
        for (var entry : mergeDeps.entrySet()) {
            mergeTargets.computeIfAbsent(entry.getValue(), key -> new ArrayList<>()).add(entry.getKey());
        }

        var handled = new HashSet<String>();
        var builder = RawMultiJarManifest.builder();

        for (var entry : rules.entrySet()) {
            var modid = entry.getKey();
            if (!handled.add(modid)) {
                continue;
            }

            var conditionSpec = entry.getValue();
            var parsed = parseConditionSpec(conditionSpec);

            var jarPaths = new ArrayList<String>();
            jarPaths.add(getJarPath(modid, context));

            var sources = mergeTargets.get(modid);
            if (sources != null) {
                for (var source : sources) {
                    if (rules.containsKey(source) && !handled.contains(source)) {
                        var sourceSpec = rules.get(source);
                        if (Objects.equals(sourceSpec, conditionSpec)) {
                            handled.add(source);
                            jarPaths.add(getJarPath(source, context));
                        }
                    }
                }
            }

            builder.addRule(new MultiJarRule<>(
                    parsed.require,
                    parsed.conflict,
                    jarPaths
            ));
        }

        mergeEntries.put(manifestPath, new ManifestEntry(builder.build()));
    }

    private record ParsedConditions(
            List<MultiJarCondition<RawVersionRange>> require,
            List<MultiJarCondition<RawVersionRange>> conflict
    ) {}

    private static ParsedConditions parseConditionSpec(String spec) {
        if (spec == null || spec.trim().isEmpty()) {
            return new ParsedConditions(Collections.emptyList(), Collections.emptyList());
        }

        var requireConditions = new LinkedHashMap<String, List<RawVersionRange>>();
        var conflictConditions = new LinkedHashMap<String, List<RawVersionRange>>();

        for (var part : spec.split(";")) {
            part = part.trim();
            if (part.isEmpty()) {
                continue;
            }

            int colonIdx = part.indexOf(':');
            if (colonIdx == -1) {
                throw new IllegalArgumentException("Missing ':' in condition: " + part);
            }

            var modid = part.substring(0, colonIdx).trim();
            var rangesStr = part.substring(colonIdx + 1).trim();

            var reqRanges = new ArrayList<RawVersionRange>();
            var confRanges = new ArrayList<RawVersionRange>();

            int i = 0;
            while (i < rangesStr.length()) {
                char c = rangesStr.charAt(i);
                if (c == '+') {
                    i++;
                    var rangeSpec = readRangeSpec(rangesStr, i);
                    reqRanges.add(new RawVersionRange(rangeSpec));
                    i += rangeSpec.length();
                } else if (c == '-') {
                    i++;
                    var rangeSpec = readRangeSpec(rangesStr, i);
                    confRanges.add(new RawVersionRange(rangeSpec));
                    i += rangeSpec.length();
                } else {
                    throw new IllegalArgumentException("Invalid version range: " + rangesStr);
                }
            }

            if (!reqRanges.isEmpty()) {
                requireConditions.computeIfAbsent(modid, k -> new ArrayList<>()).addAll(reqRanges);
            }
            if (!confRanges.isEmpty()) {
                conflictConditions.computeIfAbsent(modid, k -> new ArrayList<>()).addAll(confRanges);
            }
        }

        var require = new ArrayList<MultiJarCondition<RawVersionRange>>();
        for (var entry : requireConditions.entrySet()) {
            require.add(new MultiJarCondition<>(entry.getKey(), entry.getValue()));
        }

        var conflict = new ArrayList<MultiJarCondition<RawVersionRange>>();
        for (var entry : conflictConditions.entrySet()) {
            conflict.add(new MultiJarCondition<>(entry.getKey(), entry.getValue()));
        }

        return new ParsedConditions(require, conflict);
    }

    private static String readRangeSpec(String s, int start) {
        if (start >= s.length()) {
            throw new IllegalArgumentException("Expected range spec at position " + start);
        }
        char open = s.charAt(start);
        if (open != '[' && open != '(') {
            throw new IllegalArgumentException("Expected '[' or '(' at position " + start + ", got: " + open);
        }
        for (int i = start + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ']' || c == ')') {
                return s.substring(start, i + 1);
            }
        }
        throw new IllegalArgumentException("Unclosed range starting at position " + start);
    }

    private String getJarPath(String modid, JarInJarPlugin.JiJContext context) {
        return context != null && context.entries().containsKey(modid)
                ? context.entries().get(modid).entryPath()
                : JarInJarPlugin.JARS_BASE_PATH + modid + ".jar";
    }

    private record ManifestEntry(RawMultiJarManifest manifest) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws IOException {
            output.write(manifest.toJson().getBytes(StandardCharsets.UTF_8));
        }
    }
}
