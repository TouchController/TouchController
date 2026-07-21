package top.fifthlight.multijar.common;

@FunctionalInterface
public interface VersionFactory<R extends VersionRange> {
    R parseRange(String spec);
}
