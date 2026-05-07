package top.fifthlight.mergetools.merger.api;

public final class ContextAttributeKey<T> {
    private final String name;

    private ContextAttributeKey(String name) {
        this.name = name;
    }

    public static <T> ContextAttributeKey<T> create(String name) {
        return new ContextAttributeKey<>(name);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "ContextAttributeKey[" + name + "]";
    }
}
