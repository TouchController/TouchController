package top.fifthlight.mergetools.merger.plugin.expectactual;

import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PluginProvider;

public class ExpectActualPluginProvider implements PluginProvider {
    @Override
    public String name() {
        return "expect_actual";
    }

    @Override
    public Plugin create() {
        return new ExpectActualPlugin();
    }
}
