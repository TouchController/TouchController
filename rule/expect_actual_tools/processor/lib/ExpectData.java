package top.fifthlight.mergetools.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public record ExpectData(@JsonProperty("interfaceName") String interfaceName,
                         @JsonProperty("constructors") Constructor[] constructors) {
    @Override
    public String toString() {
        return "ExpectData{" +
                "interfaceName='" + interfaceName + '\'' +
                ", constructors=" + Arrays.toString(constructors) +
                '}';
    }

    public record Constructor(@JsonProperty("name") String name, @JsonProperty("parameters") Parameter[] parameters) {
        @Override
        public String toString() {
            return "Constructor{" +
                    "name='" + name + '\'' +
                    ", parameters=" + Arrays.toString(parameters) +
                    '}';
        }

        public record Parameter(@JsonProperty("type") String type, @JsonProperty("name") String name) {
        }
    }
}
