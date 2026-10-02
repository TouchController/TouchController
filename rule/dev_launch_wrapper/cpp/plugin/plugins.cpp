#include "plugins.h"

#include <memory>

#include "assets_plugin.h"
#include "classpath_plugin.h"
#include "copy_file_plugin.h"
#include "fml_plugin.h"
#include "game_args_plugin.h"
#include "ignore_list_plugin.h"
#include "main_class_plugin.h"
#include "module_path_plugin.h"
#include "type_plugin.h"

std::array<std::unique_ptr<Plugin>, 9> plugins = {
    std::make_unique<ClasspathPlugin>(),  std::make_unique<ModulePathPlugin>(), std::make_unique<MainClassPlugin>(),
    std::make_unique<GameArgsPlugin>(),   std::make_unique<FMLPlugin>(),        std::make_unique<AssetsPlugin>(),
    std::make_unique<IgnoreListPlugin>(), std::make_unique<TypePlugin>(),       std::make_unique<CopyFilePlugin>(),
};
