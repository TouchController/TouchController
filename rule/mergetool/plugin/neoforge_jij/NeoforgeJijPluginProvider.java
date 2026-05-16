package top.fifthlight.mergetools.merger.plugin.neoforgejij;

import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PluginProvider;

public class NeoforgeJijPluginProvider implements PluginProvider {
    @Override
    public String name() {
        return "neoforge_jij";
    }

    @Override
    public Plugin create() {
        return new NeoforgeJijPlugin();
    }
}
