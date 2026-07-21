package top.fifthlight.multijar.common.maven385;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import top.fifthlight.multijar.common.ComparableVersionItem;

import java.util.Objects;

public final class MavenVersionItem implements ComparableVersionItem<MavenVersionItem> {
    private final ArtifactVersion delegate;

    public MavenVersionItem(ArtifactVersion delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public ArtifactVersion delegate() {
        return delegate;
    }

    @Override
    public int compareTo(MavenVersionItem other) {
        return delegate.compareTo(other.delegate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MavenVersionItem that = (MavenVersionItem) o;
        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(delegate);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
