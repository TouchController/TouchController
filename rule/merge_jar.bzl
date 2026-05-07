"""Rules for merging JAR files."""

load("@rules_java//java:defs.bzl", "JavaInfo", "java_common")

def merge_jar_action(actions, executable, output_jar, jars = depset(), resources = {}):
    args = actions.args()

    args.add(output_jar)

    args.add("--manifest-mode")
    args.add("use-last-by-alphabet")

    resource_files = []
    for resource, strip in resources.items():
        files = resource.files.to_list()
        resource_files = resource_files + files
        args.add("--resource-strip")
        args.add(strip)
        if len(files) == 0:
            fail("Resource label without resource: " + str(resource.label))
        for file in files:
            args.add("--resource")
            args.add(file)
    args.add_all(jars)

    args.use_param_file("@%s", use_always = True)
    args.set_param_file_format("multiline")

    actions.run(
        inputs = depset(
            direct = resource_files,
            transitive = [jars],
        ),
        outputs = [output_jar],
        executable = executable,
        execution_requirements = {
            "supports-workers": "1",
            "supports-multiplex-workers": "1",
            "supports-multiplex-sandboxing": "1",
            "requires-worker-protocol": "proto",
        },
        arguments = [args],
        mnemonic = "MergeJar",
        toolchain = "@bazel_tools//tools/jdk:toolchain_type",
    )

def _merge_jar_impl(ctx):
    merged_deps = java_common.merge([dep[JavaInfo] for dep in ctx.attr.deps])

    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")
    merge_jar_action(
        ctx.actions,
        ctx.executable._merge_jar_executable,
        output_jar,
        merged_deps.full_compile_jars,
        ctx.attr.resources,
    )

    return [
        JavaInfo(
            output_jar = output_jar,
            compile_jar = output_jar,
        ),
        DefaultInfo(files = depset([output_jar])),
    ]

merge_jar = rule(
    implementation = _merge_jar_impl,
    attrs = {
        "deps": attr.label_list(
            mandatory = True,
            providers = [JavaInfo],
            doc = "Input JARs to be merged",
        ),
        "resources": attr.label_keyed_string_dict(
            mandatory = False,
            allow_files = True,
            default = {},
            doc = "Resource to be merged, with perfix to strip",
        ),
        "_merge_jar_executable": attr.label(
            default = "@//rule/mergetool:core",
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Merge JARs",
)
