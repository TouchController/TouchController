package top.fifthlight.multijar.common;

import java.util.Objects;

public final class RawVersionRange implements VersionRange {
    private final String spec;

    public RawVersionRange(String spec) {
        this.spec = Objects.requireNonNull(spec);
    }

    public String spec() {
        return spec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RawVersionRange that = (RawVersionRange) o;
        return Objects.equals(spec, that.spec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spec);
    }

    @Override
    public String toString() {
        return spec;
    }
}
