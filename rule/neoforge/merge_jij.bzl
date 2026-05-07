"""Rules for merging JAR files as jar-in-jar for NeoForge mods."""

load("@rules_java//java/common:java_info.bzl", "JavaInfo")

def do_merge_neoforge_jij(ctx, input_jar, output_jar, deps, executable, label):
    """Perform the actual JIJ (Jar-in-Jar) merge operation for NeoForge mods.

    Args:
        ctx: The rule context.
        input_jar: The input JAR file to merge into.
        output_jar: The output JAR file path.
        deps: Dictionary of dependency labels to their group:artifact:version:fmlType.
        executable: The merge tool executable.
        label: The label for progress messages.
    """
    args = ctx.actions.args()
    args.add(output_jar)
    args.add(input_jar)

    args.add("--jij-base-path")
    args.add("META-INF/jarjar/")

    dep_files = []
    for dep in deps.keys():
        description = deps[dep]
        jar = dep.files.to_list()[0]
        dep_files.append(jar)

        parts = description.split(":")
        group = parts[0]
        artifact = parts[1]
        version = parts[2]
        fml_type = parts[3] if len(parts) > 3 else ""
        id = group + "_" + artifact + "_" + version

        args.add("--jar-in-jar")
        args.add(id)
        args.add(jar)

        args.add("--jij-neoforge")
        args.add(id)
        args.add(description)

    args.use_param_file("@%s")
    args.set_param_file_format("multiline")

    ctx.actions.run(
        inputs = depset(dep_files + [input_jar]),
        outputs = [output_jar],
        executable = executable,
        arguments = [args],
        progress_message = "Create jij JAR %s" % label.name,
        toolchain = "@bazel_tools//tools/jdk:toolchain_type",
    )

def _neoforge_merge_jij_impl(ctx):
    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")
    input_jar = ctx.files.input[0]

    do_merge_neoforge_jij(
        ctx,
        input_jar,
        output_jar,
        ctx.attr.deps,
        ctx.executable._jij_merger_executable,
        ctx.label,
    )

    return [
        JavaInfo(
            output_jar = output_jar,
            compile_jar = output_jar,
        ),
        DefaultInfo(files = depset([output_jar])),
    ]

neoforge_merge_jij = rule(
    implementation = _neoforge_merge_jij_impl,
    attrs = {
        "input": attr.label(
            mandatory = True,
            providers = [DefaultInfo],
            doc = "Input JAR",
        ),
        "deps": attr.label_keyed_string_dict(
            mandatory = True,
            allow_files = [".jar"],
            doc = "JARs to be merged as jar-in-jar",
        ),
        "_jij_merger_executable": attr.label(
            default = Label("@//rule/mergetool:neoforge_jij"),
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Merge JAR to a main JAR as NeoForge jar-in-jar",
)
