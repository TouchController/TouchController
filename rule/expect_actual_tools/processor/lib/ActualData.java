package top.fifthlight.mergetools.processor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public record ActualData(@JsonProperty("implementationName") String implementationName,
                         @JsonProperty("spiFactoryName") String spiFactoryName,
                         @JsonProperty("constructors") Constructor[] constructors) {
    @Override
    public String toString() {
        return "ActualData{" +
                "implementationName='" + implementationName + '\'' +
                ", constructors=" + Arrays.toString(constructors) +
                '}';
    }

    public record Constructor(@JsonProperty("type") Type type,
                              @JsonProperty("name") String name,
                              @JsonProperty("parameters") Parameter[] parameters,
                              @JsonProperty("returnType") String returnType) {
        @Override
        public String toString() {
            return "Constructor{" +
                    "type=" + type +
                    ", name='" + name + '\'' +
                    ", parameters=" + Arrays.toString(parameters) +
                    '}';
        }

        public enum Type {
            CONSTRUCTOR("constructor"),
            STATIC_METHOD("static_method");

            public final String value;

            Type(String value) {
                this.value = value;
            }

            @JsonValue
            public String value() {
                return value;
            }
        }

        public record Parameter(@JsonProperty("type") String type, @JsonProperty("name") String name) {
        }
    }
}
