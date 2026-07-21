package top.fifthlight.multijar.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MultiJarRule<R extends VersionRange> {
    private final List<MultiJarCondition<R>> require;
    private final List<MultiJarCondition<R>> conflict;
    private final List<String> jarPaths;

    public MultiJarRule(
            List<MultiJarCondition<R>> require,
            List<MultiJarCondition<R>> conflict,
            List<String> jarPaths
    ) {
        this.require = Collections.unmodifiableList(new ArrayList<>(require));
        this.conflict = Collections.unmodifiableList(new ArrayList<>(conflict));
        this.jarPaths = Collections.unmodifiableList(new ArrayList<>(jarPaths));
    }

    public List<MultiJarCondition<R>> require() {
        return require;
    }

    public List<MultiJarCondition<R>> conflict() {
        return conflict;
    }

    public List<String> jarPaths() {
        return jarPaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiJarRule<?> that = (MultiJarRule<?>) o;
        return Objects.equals(require, that.require)
                && Objects.equals(conflict, that.conflict)
                && Objects.equals(jarPaths, that.jarPaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(require, conflict, jarPaths);
    }

    @Override
    public String toString() {
        return "Rule{require=" + require + ", conflict=" + conflict + ", jarPaths=" + jarPaths + '}';
    }
}
