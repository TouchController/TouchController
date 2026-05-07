package top.fifthlight.mergetools.merger.plugin.jarinjar;

import top.fifthlight.mergetools.merger.api.AttributeEnvironment;
import top.fifthlight.mergetools.merger.api.ContextAttributeKey;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class JarInJarPlugin implements Plugin {
    public static final String JARS_BASE_PATH = "META-INF/jars/";

    public static final ContextAttributeKey<JiJContext> JIJ_CONTEXT =
            ContextAttributeKey.create("jij-context");

    private String basePath = JARS_BASE_PATH;
    private final LinkedHashMap<String, Path> entries = new LinkedHashMap<>();

    @Override
    public int priority() {
        return 400;
    }

    @Override
    public boolean processArg(String arg, PreprocessEnvironment environment) {
        return switch (arg) {
            case "--jij-base-path" -> {
                basePath = environment.readNextArg();
                yield true;
            }
            case "--jar-in-jar" -> {
                var id = environment.readNextArg();
                var path = environment.resolvePath(Path.of(environment.readNextArg()));
                entries.put(id, path);
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries, AttributeEnvironment environment) {
        var ctx = new JiJContext(basePath, new LinkedHashMap<>());
        for (var entry : entries.entrySet()) {
            var id = entry.getKey();
            var sourcePath = entry.getValue();
            var entryPath = basePath + id + ".jar";
            ctx.entries().put(id, new JiJEntry(id, sourcePath, entryPath));
            mergeEntries.put(entryPath, new RawJarMergeEntry(sourcePath));
        }
        environment.putAttribute(JIJ_CONTEXT, ctx);
    }

    public record JiJContext(String basePath, LinkedHashMap<String, JiJEntry> entries) {}

    public record JiJEntry(String id, Path sourcePath, String entryPath) {}

    private record RawJarMergeEntry(Path sourcePath) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws Exception {
            Files.copy(sourcePath, output);
        }
    }
}
