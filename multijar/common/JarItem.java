package top.fifthlight.multijar.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class JarItem {
    private final List<String> jarPaths;

    public JarItem(List<String> jarPaths) {
        this.jarPaths = Collections.unmodifiableList(new ArrayList<>(jarPaths));
    }

    public JarItem(String jarPath) {
        this(Collections.singletonList(jarPath));
    }

    public List<String> jarPaths() {
        return jarPaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JarItem jarItem = (JarItem) o;
        return Objects.equals(jarPaths, jarItem.jarPaths);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(jarPaths);
    }

    @Override
    public String toString() {
        return jarPaths.toString();
    }
}
