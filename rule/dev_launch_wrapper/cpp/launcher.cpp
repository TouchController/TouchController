#include <jni.h>

#include <algorithm>
#include <cstdlib>
#include <iostream>
#include <memory>
#include <string>
#include <utility>
#include <vector>

#include "context.h"
#include "plugin.h"
#include "plugin/plugins.h"
#include "rules_cc/cc/runfiles/runfiles.h"

using rules_cc::cc::runfiles::Runfiles;

static void call_main(LaunchContext& ctx) {
    std::cerr << "JVM arguments:";
    for (const auto& flag : ctx.jvm_flags) {
        std::cerr << " " << flag;
    }
    std::cerr << std::endl;

    std::vector<JavaVMOption> options(ctx.jvm_flags.size());
    std::transform(ctx.jvm_flags.begin(), ctx.jvm_flags.end(), options.begin(), [](auto& flag) {
        JavaVMOption opt;
        opt.optionString = const_cast<char*>(flag.c_str());
        return opt;
    });

    JavaVMInitArgs vm_args;
    vm_args.version = JNI_VERSION_1_8;
    vm_args.nOptions = options.size();
    vm_args.options = options.data();
    vm_args.ignoreUnrecognized = 0;

    JavaVM* jvm;
    JNIEnv* env;
    int result = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
    if (result != JNI_OK) {
        std::cerr << "Failed to create JVM: " << result << std::endl;
        std::exit(1);
    }

    auto main_class = ctx.main_class;
    std::replace(main_class.begin(), main_class.end(), '.', '/');

    jclass clazz = env->FindClass(main_class.c_str());
    if (clazz == nullptr) {
        std::cerr << "Could not find class: " << ctx.main_class << std::endl;
        env->ExceptionDescribe();
        jvm->DestroyJavaVM();
        std::exit(1);
    }

    jmethodID main_method = env->GetStaticMethodID(clazz, "main", "([Ljava/lang/String;)V");
    if (main_method == nullptr) {
        std::cerr << "Could not find main method in class: " << ctx.main_class << std::endl;
        env->ExceptionDescribe();
        jvm->DestroyJavaVM();
        std::exit(1);
    }

    std::cerr << "Launching game with arguments:";
    for (const auto& a : ctx.program_args) {
        std::cerr << " " << a;
    }
    std::cerr << std::endl;

    jclass str_class = env->FindClass("java/lang/String");
    jobjectArray args = env->NewObjectArray(ctx.program_args.size(), str_class, nullptr);
    for (size_t i = 0; i < ctx.program_args.size(); i++) {
        jstring str = env->NewStringUTF(ctx.program_args[i].c_str());
        env->SetObjectArrayElement(args, i, str);
        env->DeleteLocalRef(str);
    }

    env->CallStaticVoidMethod(clazz, main_method, args);

    env->DeleteLocalRef(args);
    env->DeleteLocalRef(str_class);
    env->DeleteLocalRef(clazz);

    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
    }

    jvm->DestroyJavaVM();
}

int main(int argc, char* argv[]) {
    std::vector<std::string> pending(argv + 1, argv + argc);

    std::string error;
    std::shared_ptr<Runfiles> runfiles(Runfiles::Create(argv[0], &error));
    if (runfiles == nullptr) {
        std::cerr << "Failed to create Runfiles: " << error << std::endl;
        return 1;
    }

    PluginContext pctx(runfiles);
    ArgumentContext actx(pending);

    std::vector<std::string> unprocessed;
    while (actx.has_next()) {
        auto arg = actx.read();
        bool consumed = false;
        for (auto& plugin : plugins) {
            if (plugin->process_arg(arg, pctx, actx)) {
                consumed = true;
                break;
            }
        }
        if (!consumed) {
            unprocessed.push_back(arg);
        }
    }

    for (auto& plugin : plugins) {
        plugin->finalize(pctx);
    }

    LaunchContext lctx(std::move(pctx));

    bool raw = false;
    for (auto& arg : unprocessed) {
        if (raw) {
            lctx.program_args.push_back(arg);
        } else if (arg == "--") {
            raw = true;
            continue;
        } else if (arg[0] == '-') {
            lctx.jvm_flags.push_back(arg);
        } else if (lctx.main_class.empty()) {
            lctx.main_class = arg;
        } else {
            lctx.program_args.push_back(arg);
        }
    }

    if (lctx.main_class.empty()) {
        std::cerr << "No main class specified" << std::endl;
        return 1;
    }

    call_main(lctx);
    return 0;
}
