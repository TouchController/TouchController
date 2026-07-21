package top.fifthlight.multijar.common;

public interface ComparableVersionFactory<V extends ComparableVersionItem<V>, R extends ComparableVersionRange<V>>
        extends VersionFactory<R> {
    V parseVersion(String version);
}
