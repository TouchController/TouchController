package top.fifthlight.mergetools.merger.plugin.jarinjar;

import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PluginProvider;

public class JarInJarPluginProvider implements PluginProvider {
    @Override
    public String name() {
        return "jar_in_jar";
    }

    @Override
    public Plugin create() {
        return new JarInJarPlugin();
    }
}
