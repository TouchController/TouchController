package top.fifthlight.mergetools.merger.plugin.services;

import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PluginProvider;

public class ServicesPluginProvider implements PluginProvider {
    @Override
    public String name() {
        return "services";
    }

    @Override
    public Plugin create() {
        return new ServicesPlugin();
    }
}
