package top.fifthlight.mergetools.merger.plugin.servicewrapper;

/// Identity of a wrapped method: its name and type descriptor.
public record MethodItem(String name, String descriptor) implements Comparable<MethodItem> {
    @Override
    public int compareTo(MethodItem o) {
        int result = name.compareTo(o.name);
        return result != 0 ? result : descriptor.compareTo(o.descriptor);
    }
}
