package top.fifthlight.mergetools.merger.api;

import java.nio.file.Path;

public interface PreprocessEnvironment extends AttributeEnvironment {
    String readNextArg();
    void putMergeEntry(String name, MergeEntry entry);
    void putManifestEntry(String key, String value);
    Path resolvePath(Path argumentPath);
}
