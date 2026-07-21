package top.fifthlight.multijar.common.maven385;

import org.apache.maven.artifact.versioning.VersionRange;
import top.fifthlight.multijar.common.ComparableVersionRange;

import java.util.Objects;

public final class MavenVersionRange implements ComparableVersionRange<MavenVersionItem> {
    private final VersionRange mavenRange;

    public MavenVersionRange(VersionRange mavenRange) {
        this.mavenRange = Objects.requireNonNull(mavenRange);
    }

    @Override
    public boolean matches(MavenVersionItem version) {
        return mavenRange.containsVersion(version.delegate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MavenVersionRange that = (MavenVersionRange) o;
        return Objects.equals(mavenRange, that.mavenRange);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mavenRange);
    }

    @Override
    public String toString() {
        return mavenRange.toString();
    }
}
