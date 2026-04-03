package top.fifthlight.mergetools.merger.plugin.resource;

import org.jetbrains.annotations.Nullable;
import top.fifthlight.mergetools.merger.api.MergeEntry;
import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PreprocessEnvironment;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourcePlugin implements Plugin {
    @Override
    public int priority() {
        return 100;
    }

    @Nullable
    private String currentStrip = null;

    private record ResourceFileEntry(Path resourceFile) implements MergeEntry {
        @Override
        public void write(OutputStream output) throws IOException {
            try (var inputStream = Files.newInputStream(resourceFile)) {
                inputStream.transferTo(output);
            }
        }
    }

    private String stripResourcePath(String arg, String filePath) {
        var entryPath = filePath;
        if (currentStrip != null) {
            entryPath = entryPath.replace('\\', '/');
            var index = entryPath.indexOf(currentStrip);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid resource path: " + entryPath + ", not matching strip: " + currentStrip);
            }
            entryPath = entryPath.substring(index + currentStrip.length());
            if (entryPath.startsWith("/")) {
                entryPath = entryPath.substring(1);
            }
        }
        return entryPath;
    }

    @Override
    public boolean processArg(String arg, PreprocessEnvironment environment) {
        return switch (arg) {
            case "--strip" -> {
                currentStrip = environment.readNextArg();
                yield true;
            }

            case "--resource" -> {
                var filePath = environment.readNextArg();
                var entryPath = stripResourcePath(arg, filePath);
                environment.putMergeEntry(entryPath, new ResourceFileEntry(environment.resolvePath(Path.of(filePath))));
                yield true;
            }

            default -> false;
        };
    }

    @Override
    public boolean processJarEntry(JarFile file, JarEntry entry, PreprocessEnvironment environment) {
        return false;
    }
}
