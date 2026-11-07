package top.fifthlight.mergetools.merger.plugin.servicewrapper;

import top.fifthlight.mergetools.merger.api.Plugin;
import top.fifthlight.mergetools.merger.api.PluginProvider;

public class ServiceWrapperPluginProvider implements PluginProvider {
    @Override
    public String name() {
        return "service_wrapper";
    }

    @Override
    public Plugin create() {
        return new ServiceWrapperPlugin();
    }
}
