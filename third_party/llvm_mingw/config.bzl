"""Configuration for LLVM-MinGW toolchain."""

load("@rules_cc//cc:action_names.bzl", "ALL_CC_LINK_ACTION_NAMES", "ALL_CPP_COMPILE_ACTION_NAMES")
load("@rules_cc//cc:cc_toolchain_config_lib.bzl", "artifact_name_pattern", "feature", "flag_group", "flag_set", "tool_path", "with_feature_set")
load("@rules_cc//cc:defs.bzl", "CcToolchainConfigInfo")
load("@rules_cc//cc/common:cc_common.bzl", "cc_common")

def _impl(ctx):
    if ctx.attr.use_wrapper:
        SDK_PATH_PREFIX = "wrapper/%s-{}%s" % (ctx.attr.triple, ctx.attr.binary_extension)
    else:
        SDK_PATH_PREFIX = "bin/%s-{}%s" % (ctx.attr.triple, ctx.attr.binary_extension)

    tool_paths = [
        tool_path(
            name = "ar",
            path = SDK_PATH_PREFIX.format("ar"),
        ),
        tool_path(
            name = "as",
            path = SDK_PATH_PREFIX.format("as"),
        ),
        tool_path(
            name = "cpp",
            path = SDK_PATH_PREFIX.format("cpp"),
        ),
        tool_path(
            name = "gcc",
            path = SDK_PATH_PREFIX.format("gcc"),
        ),
        tool_path(
            name = "g++",
            path = SDK_PATH_PREFIX.format("g++"),
        ),
        tool_path(
            name = "gcov",
            path = SDK_PATH_PREFIX.format("gcov"),
        ),
        tool_path(
            name = "ld",
            path = SDK_PATH_PREFIX.format("ld"),
        ),
        tool_path(
            name = "nm",
            path = SDK_PATH_PREFIX.format("nm"),
        ),
        tool_path(
            name = "objcopy",
            path = SDK_PATH_PREFIX.format("objcopy"),
        ),
        tool_path(
            name = "objdump",
            path = SDK_PATH_PREFIX.format("objdump"),
        ),
        tool_path(
            name = "ranlib",
            path = SDK_PATH_PREFIX.format("ranlib"),
        ),
        tool_path(
            name = "strip",
            path = SDK_PATH_PREFIX.format("strip"),
        ),
    ]

    opt_feature = feature(name = "opt")

    features = [
        feature(
            name = "default_compile_actions",
            enabled = True,
            flag_sets = [
                flag_set(
                    actions = ALL_CPP_COMPILE_ACTION_NAMES,
                    flag_groups = ([
                        flag_group(
                            flags = [
                                "-gcodeview",
                                # Reproducibility
                                "-Wno-builtin-macro-redefined",
                                "-D__DATE__=\"redacted\"",
                                "-D__TIMESTAMP__=\"redacted\"",
                                "-D__TIME__=\"redacted\"",
                                "-no-canonical-prefixes",
                            ],
                        ),
                    ]),
                ),
                flag_set(
                    actions = ALL_CPP_COMPILE_ACTION_NAMES,
                    flag_groups = ([
                        flag_group(
                            flags = ["-O2", "-g"],
                        ),
                    ]),
                    with_features = [with_feature_set(features = ["opt"])],
                ),
            ],
        ),
        feature(
            name = "default_link_flags",
            enabled = True,
            flag_sets = [
                flag_set(
                    actions = ALL_CC_LINK_ACTION_NAMES,
                    flag_groups = ([
                        flag_group(
                            flags = [
                                "-Wl,--pdb=",
                                "-Wl,--no-insert-timestamp",
                                "-lstdc++",
                                "-lm",
                                "-static",
                            ],
                        ),
                    ]),
                ),
            ],
        ),
        opt_feature,
    ]

    return cc_common.create_cc_toolchain_config_info(
        ctx = ctx,
        features = features,
        cxx_builtin_include_directories = [
            ctx.attr.execroot + "/include",
            ctx.attr.execroot + "/" + ctx.attr.triple + "/include",
            ctx.attr.execroot + "/lib/clang/22/include",
        ],
        toolchain_identifier = "llvm-mingw",
        host_system_name = "local",
        target_system_name = "local",
        target_cpu = ctx.attr.target_cpu,
        target_libc = "unknown",
        compiler = "llvm",
        abi_version = "unknown",
        abi_libc_version = "unknown",
        tool_paths = tool_paths,
        artifact_name_patterns = [
            artifact_name_pattern(
                category_name = "static_library",
                prefix = "",
                extension = ".lib",
            ),
            artifact_name_pattern(
                category_name = "dynamic_library",
                prefix = "dynamic_",
                extension = ".dll",
            ),
            artifact_name_pattern(
                category_name = "executable",
                prefix = "",
                extension = ".exe",
            ),
        ],
    )

config = rule(
    implementation = _impl,
    attrs = {
        "triple": attr.string(mandatory = True),
        "target_cpu": attr.string(mandatory = True),
        "execroot": attr.string(mandatory = True),
        "c_opts": attr.string_list(mandatory = False, default = []),
        "link_opts": attr.string_list(mandatory = False, default = []),
        "binary_extension": attr.string(mandatory = True),
        "use_wrapper": attr.bool(default = True),
    },
    provides = [CcToolchainConfigInfo],
)
