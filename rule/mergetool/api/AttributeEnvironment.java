package top.fifthlight.mergetools.merger.api;

public interface AttributeEnvironment {
    <K extends ContextAttributeKey<T>, T> T getAttribute(K key);
    <K extends ContextAttributeKey<T>, T> void putAttribute(K key, T value);
}
