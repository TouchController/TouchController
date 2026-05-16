package top.fifthlight.mergetools.merger.api;

public interface PluginProvider {
    String name();

    Plugin create();
}
