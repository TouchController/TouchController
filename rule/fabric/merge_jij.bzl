"""Rules for merging JAR files as jar-in-jar for Fabric mods."""

load("@rules_java//java/common:java_info.bzl", "JavaInfo")

def do_merge_fabric_jij(ctx, input_jar, output_jar, deps, executable, label):
    """Perform the actual JIJ (Jar-in-Jar) merge operation for Fabric mods.

    Args:
        ctx: The rule context.
        input_jar: The input JAR file to merge into.
        output_jar: The output JAR file path.
        deps: Dictionary of dependency labels to their JAR name:version.
        executable: The merge tool executable.
        label: The label for progress messages.
    """
    args = ctx.actions.args()
    args.add("--plugin", "manifest")
    args.add("--plugin", "services")
    args.add("--plugin", "resource")
    args.add("--plugin", "jar_in_jar")
    args.add("--plugin", "fabric_jij")
    args.add(output_jar)
    args.add(input_jar)

    args.add("--jij-base-path")
    args.add("META-INF/jars/")

    dep_files = []
    for dep in deps.keys():
        name = deps[dep]
        jar = dep.files.to_list()[0]
        dep_files.append(jar)

        parts = name.split(":", 1)
        modid = parts[0]
        version = parts[1] if len(parts) > 1 else "="

        args.add("--jar-in-jar")
        args.add(modid)
        args.add(jar)

        args.add("--jij-fabric")
        args.add(modid)
        args.add(version)

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

def _fabric_merge_jij_impl(ctx):
    output_jar = ctx.actions.declare_file(ctx.label.name + ".jar")
    input_jar = ctx.files.input[0]

    do_merge_fabric_jij(
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

fabric_merge_jij = rule(
    implementation = _fabric_merge_jij_impl,
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
            default = Label("@//rule/mergetool:merger"),
            executable = True,
            cfg = "exec",
        ),
    },
    doc = "Merge JAR to a main JAR as Fabric jar-in-jar",
)

def _version_included(versions, dep_versions):
    for version in dep_versions:
        if version in versions:
            return True
    return False

def versioned_fabric_merge_jij(name, deps, versions, deps_versions, **kwargs):
    """Merge a Fabric mod JAR while filtering version-specific jar-in-jar dependencies.

    Args:
        name: Target name.
        deps: Label-keyed dict of JAR descriptions, as for fabric_merge_jij.
        versions: Game versions this JAR is built for.
        deps_versions: Dep key -> supported game version list. Keys may be labels or label strings.
    """
    if deps_versions and not versions:
        fail("versioned_fabric_merge_jij: deps_versions requires a non-empty versions list")

    version_set = {version: True for version in versions}
    normalized_deps_versions = {str(key): value for key, value in deps_versions.items()}
    filtered_deps = {}
    for dep, description in deps.items():
        dep_versions = normalized_deps_versions.get(str(dep))
        if dep_versions == None or _version_included(version_set, dep_versions):
            filtered_deps[dep] = description

    fabric_merge_jij(name = name, deps = filtered_deps, **kwargs)
