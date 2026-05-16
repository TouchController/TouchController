package top.fifthlight.mergetools.merger.plugin.manifest;

import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PluginProvider;

public class ManifestPluginProvider implements PluginProvider {
    @Override
    public String name() {
        return "manifest";
    }

    @Override
    public Plugin create() {
        return new ManifestPlugin();
    }
}
