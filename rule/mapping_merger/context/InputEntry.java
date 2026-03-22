package top.fifthlight.fabazel.mappingmerger.context;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

public record InputEntry(@NotNull Path path, @NotNull MappingFormat format,
                         @NotNull Map<String, String> namespaceMapping) {
}
