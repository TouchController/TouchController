#include "fml_plugin.h"

#include <string_view>

bool FMLPlugin::process_arg(const std::string& arg, PluginContext& pctx, ArgumentContext& actx) {
    constexpr std::string_view neoFormPrefix = "-Ddev.launch.neoFormVersion=";
    constexpr std::string_view neoForgePrefix = "-Ddev.launch.neoForgeVersion=";
    constexpr std::string_view fmlPrefix = "-Ddev.launch.fmlVersion=";
    constexpr std::string_view legacyForgePrefix = "-Ddev.launch.forgeVersion=";
    constexpr std::string_view mcpPrefix = "-Ddev.launch.mcpVersion=";
    if (arg.starts_with(neoFormPrefix)) {
        has_fml = true;
        pctx.append_program_args({"--fml.neoFormVersion", arg.substr(neoFormPrefix.size())});
        return true;
    } else if (arg.starts_with(neoForgePrefix)) {
        has_fml = true;
        pctx.append_program_args({"--fml.neoForgeVersion", arg.substr(neoForgePrefix.size())});
        return true;
    } else if (arg.starts_with(fmlPrefix)) {
        has_fml = true;
        pctx.append_program_args({"--fml.fmlVersion", arg.substr(fmlPrefix.size())});
        return true;
    } else if (arg.starts_with(legacyForgePrefix)) {
        has_fml = true;
        pctx.append_program_args({"--fml.forgeVersion", arg.substr(legacyForgePrefix.size())});
        return true;
    } else if (arg.starts_with(mcpPrefix)) {
        has_fml = true;
        pctx.append_program_args({"--fml.mcpVersion", arg.substr(mcpPrefix.size())});
        return true;
    } else {
        return false;
    }
}

void FMLPlugin::finalize(PluginContext& pctx) {
    if (!has_fml) {
        return;
    }
    auto version = pctx.attributes["version"];
    if (!version.empty()) {
        pctx.append_program_args({"--fml.mcVersion", version});
    }
}
