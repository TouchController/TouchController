package top.fifthlight.mergetools.merger.plugin.unifiedjar;

import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PluginProvider;

public class UnifiedJarManifestPluginProvider implements PluginProvider {
    @Override
    public String name() {
        return "unified_jar";
    }

    @Override
    public Plugin create() {
        return new UnifiedJarManifestPlugin();
    }
}
