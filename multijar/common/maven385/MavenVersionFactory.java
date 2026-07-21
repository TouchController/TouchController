package top.fifthlight.multijar.common.maven385;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import top.fifthlight.multijar.common.ComparableVersionFactory;

public final class MavenVersionFactory implements ComparableVersionFactory<MavenVersionItem, MavenVersionRange> {
    public static final MavenVersionFactory INSTANCE = new MavenVersionFactory();

    private MavenVersionFactory() {}

    @Override
    public MavenVersionItem parseVersion(String version) {
        return new MavenVersionItem(new DefaultArtifactVersion(version));
    }

    @Override
    public MavenVersionRange parseRange(String spec) {
        try {
            return new MavenVersionRange(VersionRange.createFromVersionSpec(spec));
        } catch (InvalidVersionSpecificationException e) {
            throw new IllegalArgumentException("Invalid version range specification: " + spec, e);
        }
    }
}
