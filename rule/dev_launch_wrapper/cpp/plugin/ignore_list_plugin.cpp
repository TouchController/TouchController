#include "ignore_list_plugin.h"

#include <algorithm>
#include <cctype>
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

static inline void rtrim(std::string& s) {
    s.erase(std::find_if(s.rbegin(), s.rend(), [](unsigned char ch) { return !std::isspace(ch); }).base(), s.end());
}

bool IgnoreListPlugin::process_arg(const std::string& arg, PluginContext& pctx, ArgumentContext& actx) {
    constexpr std::string_view prefix = "-Ddev.launch.ignoreList=";
    if (arg.starts_with(prefix)) {
        auto path = arg.substr(prefix.size());
        auto resolved = pctx.resolve_runfile(path);
        auto content = read_file(resolved);
        rtrim(content);
        pctx.append_jvm_flag("-DignoreList=" + content);
        return true;
    } else {
        return false;
    }
}
