package top.fifthlight.mergetools.merger.plugin.fabricjij;

import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PluginProvider;

public class FabricJijPluginProvider implements PluginProvider {
    @Override
    public String name() {
        return "fabric_jij";
    }

    @Override
    public Plugin create() {
        return new FabricJijPlugin();
    }
}
