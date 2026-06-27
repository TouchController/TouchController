package top.fifthlight.multijar.common;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MultiJarManifest {
    public static final String NEOFORGE_MANIFEST_PATH = "META-INF/jars/multijar-neoforge-manifest.json";
    public static final String FORGE_MANIFEST_PATH = "META-INF/jars/multijar-forge-manifest.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private static final String COMMON_KEY = "common";

    private final Map<String, List<JarItem>> entries;

    private MultiJarManifest(@NonNull Map<String, List<JarItem>> entries) {
        this.entries = entries;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    public static MultiJarManifest fromJson(@NonNull Reader reader) throws IOException {
        try (JsonReader jsonReader = GSON.newJsonReader(reader)) {
            jsonReader.beginObject();
            Map<String, List<JarItem>> result = new LinkedHashMap<>();
            while (jsonReader.hasNext()) {
                String version = jsonReader.nextName();
                List<JarItem> items = new ArrayList<>();
                jsonReader.beginArray();
                JsonToken entryToken;
                while ((entryToken = jsonReader.peek()) != JsonToken.END_ARRAY) {
                    switch (entryToken) {
                        case STRING:
                            items.add(new JarItem(jsonReader.nextString()));
                            break;
                        case BEGIN_ARRAY:
                            List<String> jarPaths = new ArrayList<>();
                            jsonReader.beginArray();
                            JsonToken arrayToken;
                            while ((arrayToken = jsonReader.peek()) != JsonToken.END_ARRAY) {
                                if (arrayToken == JsonToken.STRING) {
                                    jarPaths.add(jsonReader.nextString());
                                } else {
                                    throw new IllegalStateException("Unexpected token in version's JAR list: " + arrayToken);
                                }
                            }
                            jsonReader.endArray();
                            items.add(new JarItem(jarPaths));
                            break;
                        default:
                            throw new IllegalStateException("Bad token in version " + version);
                    }
                }
                jsonReader.endArray();
                result.put(version, items);
            }
            jsonReader.endObject();
            return new MultiJarManifest(result);
        }
    }

    @NonNull
    public String toJson() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, List<JarItem>> entry : entries.entrySet()) {
            JsonArray array = new JsonArray();
            for (JarItem item : entry.getValue()) {
                if (item.jarPaths.size() == 1) {
                    array.add(new JsonPrimitive(item.jarPaths.get(0)));
                } else {
                    JsonArray inner = new JsonArray();
                    for (String path : item.jarPaths) {
                        inner.add(new JsonPrimitive(path));
                    }
                    array.add(inner);
                }
            }
            root.add(entry.getKey(), array);
        }
        return GSON.toJson(root);
    }

    @NonNull
    public List<JarItem> common() {
        return entries.getOrDefault(COMMON_KEY, Collections.emptyList());
    }

    @NonNull
    public List<JarItem> forVersion(@NonNull String mcVersion) {
        return entries.getOrDefault(mcVersion, Collections.emptyList());
    }

    @NonNull
    public List<JarItem> items(@Nullable String mcVersion) {
        if (mcVersion == null) {
            return common();
        }
        return Stream.concat(common().stream(), forVersion(mcVersion).stream()).collect(Collectors.toList());
    }

    @NonNull
    public Map<String, List<JarItem>> entries() {
        return Collections.unmodifiableMap(entries);
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    @NonNull
    public Set<String> versions() {
        return Collections.unmodifiableSet(entries.keySet());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MultiJarManifest manifest = (MultiJarManifest) o;
        return Objects.equals(entries, manifest.entries);
    }

    @Override
    public String toString() {
        return "MultiJarManifest{entries=" + entries + '}';
    }

    public static final class Builder {
        private final Map<String, List<JarItem>> entries = new LinkedHashMap<>();

        public Builder addEntry(@NonNull String version, @NonNull JarItem item) {
            entries.computeIfAbsent(version, key -> new ArrayList<>()).add(item);
            return this;
        }

        @NonNull
        public MultiJarManifest build() {
            return new MultiJarManifest(entries);
        }
    }

    public static final class JarItem {
        private final List<String> jarPaths;

        public JarItem(List<String> jarPaths) {
            this.jarPaths = jarPaths;
        }

        public JarItem(String jarPath) {
            this(Collections.singletonList(jarPath));
        }

        public List<String> jarPaths() {
            return jarPaths;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            JarItem that = (JarItem) obj;
            return Objects.equals(this.jarPaths, that.jarPaths);
        }

        @Override
        public int hashCode() {
            return jarPaths.hashCode();
        }

        @Override
        public String toString() {
            return jarPaths.toString();
        }
    }
}
