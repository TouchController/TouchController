package top.fifthlight.fabazel.modrinthuploader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.Objects;

public record ModrinthUploadData(
        @JsonProperty("name") String name,
        @JsonProperty("version_number") String versionNumber,
        @JsonProperty("changelog") String changelog,
        @JsonProperty("dependencies") List<Dependency> dependencies,
        @JsonProperty("game_versions") List<String> gameVersions,
        @JsonProperty("version_type") String versionType,
        @JsonProperty("loaders") List<String> loaders,
        @JsonProperty("project_id") String projectId,
        @JsonProperty("file_parts") List<String> fileParts,
        @JsonProperty("primary_file") String primaryFile,
        @JsonProperty("featured") boolean featured
) {
    public ModrinthUploadData {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(versionNumber, "versionNumber cannot be null");
        Objects.requireNonNull(gameVersions, "gameVersions list cannot be null");
        if (gameVersions.isEmpty()) {
            throw new IllegalArgumentException("gameVersions list cannot be empty");
        }
        Objects.requireNonNull(versionType, "versionType cannot be null");
        Objects.requireNonNull(loaders, "loaders list cannot be null");
        if (loaders.isEmpty()) {
            throw new IllegalArgumentException("loaders list cannot be empty");
        }
        Objects.requireNonNull(projectId, "projectId cannot be null");
        Objects.requireNonNull(fileParts, "fileParts list cannot be null");
        if (fileParts.isEmpty()) {
            throw new IllegalArgumentException("fileParts list cannot be empty");
        }
        Objects.requireNonNull(primaryFile, "primaryFile cannot be null");
    }

    public record Dependency(@JsonProperty("project_id") String projectId,
                             @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("version_id") String versionId,
                             @JsonProperty("dependency_type") Type type) {
        public Dependency {
            Objects.requireNonNull(projectId, "projectId cannot be null");
            Objects.requireNonNull(type, "type cannot be null");
        }

        public static Dependency parse(String dependency) {
            var parts = dependency.split(":");
            if (parts.length != 2 && parts.length != 3) {
                throw new IllegalArgumentException("Invalid dependency: " + dependency);
            }
            var type = ModrinthUploadData.Dependency.Type.fromName(parts[0]);
            var dependencyProjectId = parts[1];
            var dependencyVersionId = parts.length == 3 ? parts[2] : null;
            return new ModrinthUploadData.Dependency(dependencyProjectId, dependencyVersionId, type);
        }

        public enum Type {
            REQUIRED,
            OPTIONAL,
            INCOMPATIBLE,
            EMBEDDED;

            @JsonValue
            public String toString() {
                return name().toLowerCase();
            }

            public static Type fromName(String name) {
                return switch (name.toLowerCase()) {
                    case "required" -> REQUIRED;
                    case "optional" -> OPTIONAL;
                    case "incompatible" -> INCOMPATIBLE;
                    case "embedded" -> EMBEDDED;
                    default -> throw new IllegalArgumentException("Unknown dependency type: " + name);
                };
            }
        }
    }
}
