"""Test rule checking libraries.conf against maven_install.json."""

load("@bazel_lib//lib:paths.bzl", "to_rlocation_path")

def _libraries_conf_test_impl(ctx):
    checker = ctx.executable._checker
    conf = ctx.file.conf
    maven_lock = ctx.file.maven_lock

    is_windows = ctx.target_platform_has_constraint(
        ctx.attr._windows_constraint[platform_common.ConstraintValueInfo],
    )

    if is_windows:
        test_suffix = "-test.bat"
        template = ctx.file._launcher_bat
    else:
        test_suffix = "-test.sh"
        template = ctx.file._launcher_sh

    test_bin = ctx.actions.declare_file(ctx.label.name + test_suffix)
    ctx.actions.expand_template(
        template = template,
        output = test_bin,
        substitutions = {
            "TEMPLATED_checker": to_rlocation_path(ctx, checker),
            "TEMPLATED_conf": to_rlocation_path(ctx, conf),
            "TEMPLATED_maven_lock": to_rlocation_path(ctx, maven_lock),
        },
        is_executable = True,
    )

    runfiles = ctx.runfiles(files = [conf, maven_lock])
    runfiles = runfiles.merge_all([
        ctx.attr._checker[DefaultInfo].default_runfiles,
        ctx.attr._bash_runfiles[DefaultInfo].default_runfiles,
    ])

    return DefaultInfo(
        executable = test_bin,
        files = depset([test_bin]),
        runfiles = runfiles,
    )

libraries_conf_test = rule(
    implementation = _libraries_conf_test_impl,
    test = True,
    attrs = {
        "conf": attr.label(
            mandatory = True,
            allow_single_file = True,
            doc = "The committed libraries.conf file.",
        ),
        "maven_lock": attr.label(
            mandatory = True,
            allow_single_file = [".json"],
            doc = "The maven_install.json lock file.",
        ),
        "_checker": attr.label(
            default = Label("//rule/aboutlibraries/checker"),
            executable = True,
            cfg = "exec",
        ),
        "_launcher_sh": attr.label(
            default = Label("//rule/aboutlibraries/checker:launcher.sh.tpl"),
            allow_single_file = True,
        ),
        "_launcher_bat": attr.label(
            default = Label("//rule/aboutlibraries/checker:launcher.bat.tpl"),
            allow_single_file = True,
        ),
        "_bash_runfiles": attr.label(
            default = "@rules_shell//shell/runfiles",
        ),
        "_windows_constraint": attr.label(
            default = "@platforms//os:windows",
        ),
    },
    doc = "Test rule that fails if libraries.conf and maven_install.json disagree on versions.",
)
