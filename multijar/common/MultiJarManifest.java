package top.fifthlight.multijar.common;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class MultiJarManifest<R extends VersionRange> {
    public static final String NEOFORGE_MANIFEST_PATH = "META-INF/jars/multijar-neoforge-manifest.json";
    public static final String FORGE_MANIFEST_PATH = "META-INF/jars/multijar-forge-manifest.json";

    private final List<MultiJarRule<R>> rules;

    protected MultiJarManifest(List<MultiJarRule<R>> rules) {
        this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
    }

    public List<MultiJarRule<R>> rules() {
        return rules;
    }

    protected static void readStringArray(JsonReader reader, List<String> target) throws IOException {
        reader.beginArray();
        while (reader.peek() != JsonToken.END_ARRAY) {
            target.add(reader.nextString());
        }
        reader.endArray();
    }

    protected static <R extends VersionRange> MultiJarRule<R> parseRule(JsonReader reader, VersionFactory<R> factory)
            throws IOException {
        List<MultiJarCondition<R>> require = new ArrayList<>();
        List<MultiJarCondition<R>> conflict = new ArrayList<>();
        List<String> jarPaths = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String fieldName = reader.nextName();
            switch (fieldName) {
                case "require":
                    readConditions(reader, require, factory);
                    break;
                case "conflict":
                    readConditions(reader, conflict, factory);
                    break;
                case "jars":
                    readStringArray(reader, jarPaths);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new MultiJarRule<>(require, conflict, jarPaths);
    }

    protected static <R extends VersionRange> void readConditions(
            JsonReader reader,
            List<MultiJarCondition<R>> target,
            VersionFactory<R> factory
    ) throws IOException {
        reader.beginArray();
        while (reader.peek() != JsonToken.END_ARRAY) {
            String modid = null;
            List<R> ranges = new ArrayList<>();

            reader.beginObject();
            while (reader.hasNext()) {
                String fieldName = reader.nextName();
                switch (fieldName) {
                    case "modid":
                        modid = reader.nextString();
                        break;
                    case "versionRange":
                        readRanges(reader, ranges, factory);
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();

            if (modid != null) {
                target.add(new MultiJarCondition<>(modid, ranges));
            }
        }
        reader.endArray();
    }

    protected static <R extends VersionRange> void readRanges(
            JsonReader reader,
            List<R> target,
            VersionFactory<R> factory
    ) throws IOException {
        JsonToken token = reader.peek();
        if (token == JsonToken.STRING) {
            target.add(factory.parseRange(reader.nextString()));
        } else if (token == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.peek() != JsonToken.END_ARRAY) {
                target.add(factory.parseRange(reader.nextString()));
            }
            reader.endArray();
        } else {
            throw new IllegalStateException("Expected string or array for versionRange, got: " + token);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiJarManifest<?> that = (MultiJarManifest<?>) o;
        return Objects.equals(rules, that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(rules);
    }

    @Override
    public String toString() {
        return "MultiJarManifest{rules=" + rules + '}';
    }
}
