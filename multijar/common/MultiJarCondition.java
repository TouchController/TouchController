package top.fifthlight.multijar.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MultiJarCondition<R extends VersionRange> {
    private final String modid;
    private final List<R> versionRanges;

    public MultiJarCondition(String modid, List<R> versionRanges) {
        this.modid = Objects.requireNonNull(modid);
        this.versionRanges = Collections.unmodifiableList(new ArrayList<>(versionRanges));
    }

    public String modid() {
        return modid;
    }

    public List<R> versionRanges() {
        return versionRanges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiJarCondition<?> that = (MultiJarCondition<?>) o;
        return Objects.equals(modid, that.modid) && Objects.equals(versionRanges, that.versionRanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modid, versionRanges);
    }

    @Override
    public String toString() {
        return "Condition{modid='" + modid + "', versionRanges=" + versionRanges + '}';
    }
}
