#pragma once

#include <initializer_list>
#include <iostream>
#include <map>
#include <string>
#include <vector>

#include "rules_cc/cc/runfiles/runfiles.h"

using rules_cc::cc::runfiles::Runfiles;

class PluginContext {
   private:
    std::shared_ptr<Runfiles> runfiles;
    std::vector<std::string> jvm_flags;
    std::vector<std::string> program_args;

   public:
    friend class LaunchContext;
    PluginContext(std::shared_ptr<Runfiles> runfiles) : runfiles(runfiles) {}

    std::string main_class;
    std::map<std::string, std::string> attributes;

    void append_jvm_flag(const std::string& arg) { jvm_flags.push_back(arg); }

    void append_jvm_flags(std::initializer_list<std::string> args) {
        jvm_flags.insert(jvm_flags.end(), args.begin(), args.end());
    }

    void append_program_arg(const std::string& arg) { program_args.push_back(arg); }

    void append_program_args(std::initializer_list<std::string> args) {
        program_args.insert(program_args.end(), args.begin(), args.end());
    }

    std::string resolve_runfile(const std::string& path) {
        if (path.starts_with("./")) {
            return path;
        }
        return runfiles->Rlocation(path);
    }
};

class LaunchContext {
   public:
    LaunchContext(PluginContext&& pctx)
        : jvm_flags(std::move(pctx.jvm_flags)),
          program_args(std::move(pctx.program_args)),
          main_class(std::move(pctx.main_class)),
          attributes(std::move(pctx.attributes)) {}

    std::vector<std::string> jvm_flags;
    std::vector<std::string> program_args;
    std::string main_class;
    std::map<std::string, std::string> attributes;
};

class ArgumentContext {
   private:
    const std::vector<std::string>& args;
    size_t index = 0;

   public:
    ArgumentContext(const std::vector<std::string>& args) : args(args) {}

    bool has_next() const { return index < args.size(); }

    std::string read() {
        if (index >= args.size()) {
            throw std::out_of_range("Unexpected end of arguments");
        }
        return args[index++];
    }
};
