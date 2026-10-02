#include "copy_file_plugin.h"

#include <stdexcept>
#include <string>
#include <string_view>
#include <filesystem>

bool CopyFilePlugin::process_arg(const std::string& arg, PluginContext& pctx, ArgumentContext& actx) {
    constexpr std::string_view prefix = "-Ddev.launch.copyFile=";
    if (!arg.starts_with(prefix)) {
        return false;
    }

    const std::string argument = arg.substr(prefix.size());
    const int separatorIndex = argument.find(":");
    if (separatorIndex == -1) {
        throw std::invalid_argument("Bad copy file: " + argument);
    }

    const std::filesystem::path src = pctx.resolve_runfile(argument.substr(0, separatorIndex));
    const std::filesystem::path dst = argument.substr(separatorIndex + 1);
    std::filesystem::remove(dst);
    std::filesystem::copy_file(src, dst, std::filesystem::copy_options::overwrite_existing);
    return true;
}
