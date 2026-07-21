package top.fifthlight.multijar.common;

public interface ComparableVersionRange<V extends ComparableVersionItem<V>> extends VersionRange {
    boolean matches(V version);
}
