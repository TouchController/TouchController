package top.fifthlight.mergetools.merger.api;

import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface Plugin extends AutoCloseable {
    default int priority() {
        return 500;
    }

    default boolean processArg(String arg, PreprocessEnvironment environment) {
        return false;
    }

    default boolean processJarEntry(JarFile file, JarEntry entry, PreprocessEnvironment environment) throws Exception {
        return false;
    }

    default void preSorting(Map<String, MergeEntry> mergeEntries, Map<String, String> manifestEntries, AttributeEnvironment environment) {}

    @Override
    default void close() {}
}
