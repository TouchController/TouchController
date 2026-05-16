package top.fifthlight.mergetools.merger.plugin.resource;

import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PluginProvider;

public class ResourcePluginProvider implements PluginProvider {
    @Override
    public String name() {
        return "resource";
    }

    @Override
    public Plugin create() {
        return new ResourcePlugin();
    }
}
