#include "assets_plugin.h"

#include <filesystem>
#include <fstream>
#include <string>
#include <string_view>

static std::string read_file(const std::filesystem::path& path) {
    std::ifstream stream;
    stream.exceptions(std::ios::failbit | std::ios::badbit);
    stream.open(path);
    return std::string((std::istreambuf_iterator<char>(stream)), std::istreambuf_iterator<char>());
}

bool AssetsPlugin::process_arg(const std::string& arg, PluginContext& pctx, ArgumentContext& actx) {
    constexpr std::string_view assetsVersionPrefix = "-Ddev.launch.assetsVersion=";
    if (arg.starts_with(assetsVersionPrefix)) {
        assets_version = pctx.resolve_runfile(arg.substr(assetsVersionPrefix.size()));
        return true;
    } else {
        return false;
    }
}

void AssetsPlugin::finalize(PluginContext& pctx) {
    if (assets_version.empty()) {
        return;
    }

    auto assets_path = std::filesystem::canonical(assets_version.parent_path().parent_path());

    pctx.append_program_args({"--assetsDir", assets_path});
    auto version = pctx.attributes["version"];
    if (!version.empty()) {
        auto version_path = assets_path / "versions" / version;
        pctx.append_program_args({"--assetIndex", read_file(version_path)});
    }
}
