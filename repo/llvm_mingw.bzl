"Module extension to download and configure LLVM-MinGW toolchains"

def _llvm_mingw_files_impl(rctx):
    rctx.download_and_extract(
        url = rctx.attr.url,
        sha256 = rctx.attr.sha256,
        strip_prefix = rctx.attr.strip_prefix,
        type = rctx.attr.type,
    )

    build_file = [
        'load("@//third_party/llvm_mingw:toolchain.bzl", "llvm_mingw_toolchain")',
        "",
        "filegroup(",
        '    name = "empty",',
        '    visibility = ["//visibility:public"],',
        ")",
    ]

    if rctx.attr.use_wrapper:
        wrapper_files = ["ar", "as", "cpp", "gcc", "g++", "gcov", "ld", "nm", "objcopy", "ranlib", "strip"]
        for triple in rctx.attr.targets.values():
            for wrapper_file in wrapper_files:
                real_tool = triple + "-" + wrapper_file
                rctx.template(
                    "wrapper/" + real_tool,
                    Label("@//third_party/llvm_mingw:wrapper.sh"),
                    substitutions = {
                        "%{REAL_TOOL}": real_tool,
                    },
                )
        build_file.append("""
filegroup(
    name = "wrapper_files",
    srcs = glob(["wrapper/*"]),
    visibility = ["//visibility:public"],
)
""")

    for name, triple in rctx.attr.targets.items():
        build_file += [
            "",
            "llvm_mingw_toolchain(",
            '    name = "%s",' % name,
            '    triple = "%s",' % triple,
            "    use_wrapper = %s," % rctx.attr.use_wrapper,
            '    binary_extension = "%s",' % rctx.attr.binary_extension,
            '    target_cpu_name = "%s",' % name,
            '    execroot = "external/%s",' % rctx.name,
            '    visibility = ["//visibility:public"],',
            ")",
        ]

    rctx.file(
        "BUILD.bazel",
        content = "\n".join(build_file),
        executable = False,
    )

_llvm_mingw_files = repository_rule(
    implementation = _llvm_mingw_files_impl,
    attrs = {
        "url": attr.string(
            doc = "URL to download toolchain from",
            mandatory = True,
        ),
        "sha256": attr.string(
            doc = "SHA256 hash of toolchain archive",
            mandatory = True,
        ),
        "strip_prefix": attr.string(
            doc = "Prefix to strip from the toolchain archive",
            mandatory = True,
        ),
        "type": attr.string(
            doc = "Archive type, such as .zip or .tar.xz",
            mandatory = True,
        ),
        "targets": attr.string_dict(
            doc = "Toolchain chain to triple mappings",
            mandatory = True,
        ),
        "binary_extension": attr.string(
            doc = "Binary extension for toolchain",
            mandatory = True,
        ),
        "use_wrapper": attr.bool(
            doc = "Use wrapper script to execute tools",
            default = True,
        ),
    },
)

def _llvm_mingw_config_impl(rctx):
    files_repo = rctx.attr.files_repo.name

    build_file = []

    for name in rctx.attr.targets.keys():
        build_file += [
            "toolchain(",
            '    name = "%s_toolchain",' % name,
            "    exec_compatible_with = %s," % [str(label) for label in rctx.attr.exec_compatible_with],
            "    target_compatible_with = [",
            '        "@platforms//os:windows",',
            '        "@platforms//cpu:%s",' % name,
            "    ],",
            "    target_settings = None,",
            '    toolchain = "@%s//:%s_cc_toolchain",' % (files_repo, name),
            '    toolchain_type = "@bazel_tools//tools/cpp:toolchain_type",',
            '    visibility = ["//visibility:public"]',
            ")",
            "",
        ]

    rctx.file(
        "BUILD.bazel",
        content = "\n".join(build_file),
        executable = False,
    )

_llvm_mingw_config = repository_rule(
    implementation = _llvm_mingw_config_impl,
    local = True,
    attrs = {
        "targets": attr.string_dict(
            doc = "Toolchain chain to triple mappings",
            mandatory = True,
        ),
        "exec_compatible_with": attr.label_list(
            doc = "Executable constraints",
            mandatory = True,
        ),
        "files_repo": attr.label(
            doc = "Files repository",
            mandatory = True,
        ),
    },
)

toolchain = tag_class(
    attrs = {
        "name": attr.string(
            doc = "Generated repository name",
            mandatory = True,
        ),
        "url": attr.string(
            doc = "URL to download toolchain from",
            mandatory = True,
        ),
        "sha256": attr.string(
            doc = "SHA256 hash of toolchain archive",
            mandatory = True,
        ),
        "strip_prefix": attr.string(
            doc = "Prefix to strip from the toolchain archive",
            mandatory = True,
        ),
        "type": attr.string(
            doc = "Archive type, such as .zip or .tar.xz",
            mandatory = True,
        ),
        "targets": attr.string_dict(
            doc = "Toolchain chain to triple mappings",
            mandatory = True,
        ),
        "exec_compatible_with": attr.label_list(
            doc = "Executable constraints",
            mandatory = True,
        ),
        "binary_extension": attr.string(
            doc = "Binary extension for toolchain",
            mandatory = True,
        ),
        "use_wrapper": attr.bool(
            doc = "Use wrapper script to execute tools",
            default = True,
        ),
    },
)

def _llvm_mingw_impl(mctx):
    toolchain_entries = {}
    for mod in mctx.modules:
        for toolchain in mod.tags.toolchain:
            if toolchain.name in toolchain_entries:
                entry = toolchain_entries[toolchain.name]
                if toolchain.url != entry.url:
                    fail("Toolchain URL mismatch for %s: %s != %s" % (toolchain.name, toolchain.url, entry.url))
                if toolchain.sha256 != entry.sha256:
                    fail("Toolchain SHA256 mismatch for %s: %s != %s" % (toolchain.name, toolchain.sha256, entry.sha256))
                if toolchain.strip_prefix != entry.strip_prefix:
                    fail("Toolchain strip_prefix mismatch for %s: %s != %s" % (toolchain.name, toolchain.strip_prefix, entry.strip_prefix))
                if toolchain.type != entry.type:
                    fail("Toolchain type mismatch for %s: %s != %s" % (toolchain.name, toolchain.type, entry.type))
                if toolchain.targets != entry.targets:
                    fail("Toolchain targets mismatch for %s: %s != %s" % (toolchain.name, toolchain.targets, entry.targets))
                if toolchain.exec_compatible_with != entry.exec_compatible_with:
                    fail("Toolchain exec_compatible_with mismatch for %s: %s != %s" % (toolchain.name, toolchain.exec_compatible_with, entry.exec_compatible_with))
                if toolchain.binary_extension != entry.binary_extension:
                    fail("Toolchain binary_extension mismatch for %s: %s != %s" % (toolchain.name, toolchain.binary_extension, entry.binary_extension))
                if toolchain.use_wrapper != entry.use_wrapper:
                    fail("Toolchain use_wrapper mismatch for %s: %s != %s" % (toolchain.name, toolchain.use_wrapper, entry.use_wrapper))
            else:
                toolchain_entries[toolchain.name] = struct(
                    url = toolchain.url,
                    sha256 = toolchain.sha256,
                    strip_prefix = toolchain.strip_prefix,
                    type = toolchain.type,
                    targets = toolchain.targets,
                    exec_compatible_with = toolchain.exec_compatible_with,
                    binary_extension = toolchain.binary_extension,
                    use_wrapper = toolchain.use_wrapper,
                )

    for name, toolchain in toolchain_entries.items():
        files_repo = name + "_files"
        _llvm_mingw_files(
            name = files_repo,
            url = toolchain.url,
            sha256 = toolchain.sha256,
            strip_prefix = toolchain.strip_prefix,
            type = toolchain.type,
            targets = toolchain.targets,
            binary_extension = toolchain.binary_extension,
            use_wrapper = toolchain.use_wrapper,
        )

        _llvm_mingw_config(
            name = name,
            targets = toolchain.targets,
            exec_compatible_with = toolchain.exec_compatible_with,
            files_repo = "@" + files_repo,
        )

llvm_mingw = module_extension(
    implementation = _llvm_mingw_impl,
    tag_classes = {
        "toolchain": toolchain,
    },
)
