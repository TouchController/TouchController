package top.fifthlight.mergetools.merger.impl;

import org.jspecify.annotations.Nullable;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PreprocessEnvironmentImpl implements PreprocessEnvironment {
    private final @Nullable Path sandboxPath;
    private final String[] args;
    private int argIndex = 0;
    private final HashMap<String, MergeEntry> mergeEntries = new HashMap<>();
    private final HashMap<String, String> manifestEntries = new HashMap<>();

    public PreprocessEnvironmentImpl(@Nullable Path sandboxPath, String[] args) {
        this.sandboxPath = sandboxPath;
        this.args = args;
    }

    public boolean hasNextArg() {
        return argIndex < args.length;
    }

    @Override
    public String readNextArg() {
        if (argIndex >= args.length) {
            throw new IllegalStateException("No more arguments");
        }
        return args[argIndex++];
    }

    @Override
    public void putMergeEntry(String name, MergeEntry entry) {
        mergeEntries.put(name, entry);
    }

    @Override
    public void putManifestEntry(String key, String value) {
        manifestEntries.put(key, value);
    }

    @Override
    public Path resolvePath(Path argumentPath) {
        return sandboxPath != null ? sandboxPath.resolve(argumentPath) : argumentPath;
    }

    public Map<String, MergeEntry> getMergeEntries() {
        return mergeEntries;
    }

    public Map<String, String> getManifestEntries() {
        return manifestEntries;
    }
}
