#pragma once

#include <string>

#include "../context.h"
#include "../plugin.h"

class CopyFilePlugin : public Plugin {
   public:
    bool process_arg(const std::string& arg, PluginContext& pctx, ArgumentContext& actx) override;
};
