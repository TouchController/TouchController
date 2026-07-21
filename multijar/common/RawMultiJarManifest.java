package top.fifthlight.multijar.common;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public final class RawMultiJarManifest extends MultiJarManifest<RawVersionRange> {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public RawMultiJarManifest(@NonNull List<MultiJarRule<RawVersionRange>> rules) {
        super(rules);
    }

    @NonNull
    public static RawMultiJarManifest fromJson(@NonNull Reader reader) throws IOException {
        try (JsonReader jsonReader = GSON.newJsonReader(reader)) {
            List<MultiJarRule<RawVersionRange>> rules = new ArrayList<>();
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if ("rules".equals(name)) {
                    jsonReader.beginArray();
                    while (jsonReader.peek() != JsonToken.END_ARRAY) {
                        rules.add(parseRule(jsonReader, RawVersionRange::new));
                    }
                    jsonReader.endArray();
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
            return new RawMultiJarManifest(rules);
        }
    }

    @NonNull
    public String toJson() {
        JsonObject root = new JsonObject();
        JsonArray rulesArray = new JsonArray();

        for (MultiJarRule<RawVersionRange> rule : rules()) {
            JsonObject ruleObj = new JsonObject();

            if (!rule.require().isEmpty()) {
                ruleObj.add("require", conditionsToJson(rule.require()));
            }
            if (!rule.conflict().isEmpty()) {
                ruleObj.add("conflict", conditionsToJson(rule.conflict()));
            }

            JsonArray jarsArray = new JsonArray();
            for (String path : rule.jarPaths()) {
                jarsArray.add(new JsonPrimitive(path));
            }
            ruleObj.add("jars", jarsArray);

            rulesArray.add(ruleObj);
        }

        root.add("rules", rulesArray);
        return GSON.toJson(root);
    }

    private static JsonArray conditionsToJson(List<MultiJarCondition<RawVersionRange>> conditions) {
        JsonArray array = new JsonArray();
        for (MultiJarCondition<RawVersionRange> condition : conditions) {
            JsonObject condObj = new JsonObject();
            condObj.addProperty("modid", condition.modid());

            List<RawVersionRange> ranges = condition.versionRanges();
            if (ranges.size() == 1) {
                condObj.addProperty("versionRange", ranges.get(0).spec());
            } else {
                JsonArray rangesArray = new JsonArray();
                for (RawVersionRange range : ranges) {
                    rangesArray.add(new JsonPrimitive(range.spec()));
                }
                condObj.add("versionRange", rangesArray);
            }

            array.add(condObj);
        }
        return array;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<MultiJarRule<RawVersionRange>> rules = new ArrayList<>();

        @NonNull
        public Builder addRule(@NonNull MultiJarRule<RawVersionRange> rule) {
            rules.add(rule);
            return this;
        }

        @NonNull
        public RawMultiJarManifest build() {
            return new RawMultiJarManifest(rules);
        }
    }
}
