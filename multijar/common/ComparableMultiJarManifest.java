package top.fifthlight.multijar.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class ComparableMultiJarManifest<
        V extends ComparableVersionItem<V>,
        R extends ComparableVersionRange<V>
        > extends MultiJarManifest<R> {
    private static final Gson GSON = new GsonBuilder().create();

    public ComparableMultiJarManifest(@NonNull List<MultiJarRule<R>> rules) {
        super(rules);
    }

    @NonNull
    public static <V extends ComparableVersionItem<V>, R extends ComparableVersionRange<V>>
    ComparableMultiJarManifest<V, R> fromJson(@NonNull Reader reader, @NonNull ComparableVersionFactory<V, R> factory) throws IOException {
        try (JsonReader jsonReader = GSON.newJsonReader(reader)) {
            List<MultiJarRule<R>> rules = new ArrayList<>();
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if ("rules".equals(name)) {
                    jsonReader.beginArray();
                    while (jsonReader.peek() != JsonToken.END_ARRAY) {
                        rules.add(parseRule(jsonReader, factory));
                    }
                    jsonReader.endArray();
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new ComparableMultiJarManifest<>(rules);
        }
    }


    @NonNull
    public List<JarItem> items(@NonNull Function<String, Optional<V>> modVersionProvider) {
        List<JarItem> result = new ArrayList<>();

        for (MultiJarRule<R> rule : rules()) {
            boolean allRequireMatch = rule.require().stream().allMatch(condition -> {
                Optional<V> version = modVersionProvider.apply(condition.modid());
                return version.filter(v -> condition.versionRanges().stream().anyMatch(range -> range.matches(v))).isPresent();
            });

            boolean anyConflictMatch = rule.conflict().stream().anyMatch(condition -> {
                Optional<V> version = modVersionProvider.apply(condition.modid());
                return version.filter(v -> condition.versionRanges().stream().anyMatch(range -> range.matches(v))).isPresent();
            });

            if (allRequireMatch && !anyConflictMatch) {
                result.add(new JarItem(rule.jarPaths()));
            }
        }
        return result;
    }
}
