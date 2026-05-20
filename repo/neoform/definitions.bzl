"""NeoForm definitions: dicts for dispatch, placeholder config, function specials, etc."""

load("@//private:maven_coordinate.bzl", _convert_maven_coordinate_to_repo = "convert_maven_coordinate_to_repo")

# ── URL constants ──────────────────────────────────────────────────────────────

_neoforge_repository_url = "https://maven.neoforged.net/releases"
_minecraftforge_repository_url = "https://maven.minecraftforge.net/releases"
_config_link = "%s/net/neoforged/neoform/%s/neoform-%s.zip"
_config_link_legacy = "%s/de/oceanlabs/mcp/mcp_config/%s/mcp_config-%s.zip"

# ── names ──────────────────────────────────────────────────────────────────────

names = {
    True: struct(
        url = _minecraftforge_repository_url,
        prefix = "mcp",
        config = _config_link_legacy,
        repo_fmt = "mcp_%s",
    ),
    False: struct(
        url = _neoforge_repository_url,
        prefix = "neoform",
        config = _config_link,
        repo_fmt = "neoform_%s",
    ),
}

# ── function_specials ──────────────────────────────────────────────────────────

def _decompile_before_args(main_class, context):
    result = []
    if context.sas_data:
        result += [
            '    sas_output = ctx.actions.declare_file("_neoform_decompile/" + ctx.label.name + "_sas.jar")',
            "    sas_args = ctx.actions.args()",
            '    sas_args.add("--strip")',
            '    sas_args.add("--input", input)',
            '    sas_args.add("--output", sas_output)',
            '    sas_args.add_all(ctx.files.sas_data, before_each = "--data")',
            "    ctx.actions.run(",
            "        inputs = [input] + ctx.files.sas_data,",
            "        outputs = [sas_output],",
            "        executable = ctx.executable._sas_bin,",
            "        arguments = [sas_args],",
            '        mnemonic = "SAS",',
            "    )",
            "    input = sas_output",
        ]
    result += [
        '    args.add("%s")' % main_class,
        "    args.add(output_file)",
    ]
    return result

def _decompile_extra_attrs(context):
    if not context.sas_data:
        return []
    return [
        '        "_sas_bin": attr.label(',
        '            default = "@//repo/neoform/rule/sas",',
        "            executable = True,",
        '            cfg = "exec",',
        "        ),",
        '        "sas_data": attr.label(',
        '            default = "%s",' % context.sas_data,
        "            allow_files = True,",
        "        ),",
    ]

default_function_special = struct(
    main_class_override = None,
    extra_runtime_deps = [],
    before_template_args = None,
    output_type = "jar",
    extra_attrs = None,
)

function_specials = {
    "decompile": struct(
        main_class_override = "DecompilerWrapper",
        extra_runtime_deps = ['"@//repo/neoform/rule/decompiler_wrapper"'],
        before_template_args = _decompile_before_args,
        output_type = "jar",
        extra_attrs = _decompile_extra_attrs,
    ),
    "mergeMappings": struct(
        main_class_override = None,
        extra_runtime_deps = [],
        before_template_args = None,
        output_type = "tsrg",
        extra_attrs = None,
    ),
}

# ── placeholder_config ─────────────────────────────────────────────────────────

def _build_file_attr(name, data_paths, output_type):
    lines = ['        "%s": attr.label(' % name]
    if name in data_paths:
        lines.append('            default = "//:%s",' % name)
    else:
        lines.append("            mandatory = True,")
    if output_type == "jar":
        lines.append("            providers = [[], [JavaSourceInfo]],")
    lines.append("            allow_single_file = True,")
    lines.append("        ),")
    return lines

def _generate_jar_list_args_code(function_name, name, output_type):
    """Generate variable declarations for a jar_list typed placeholder"""
    input_depsets_name = "input_%s_depsets" % name
    input_files_name = "input_%s_files" % name
    input_paths_name = "input_%s_paths" % name
    input_paths_path_name = "input_%s_paths_path" % name
    input_paths_file_name = "input_%s_paths_path_file" % name

    code = []
    code.append('    %s = "_neoform_%s/" + ctx.label.name + "_%s_libraries.txt"' % (input_paths_path_name, function_name, name))
    code.append("    %s = []" % input_depsets_name)
    code.append("    for attr in ctx.attr.%s:" % name)
    code.append("        %s.append(attr[JavaInfo].full_compile_jars)" % input_depsets_name)
    code.append("    %s = depset(transitive = %s).to_list()" % (input_files_name, input_depsets_name))
    if output_type == "jar":
        code.append("    input_libraries += %s" % input_files_name)
    code.append("    %s = []" % input_paths_name)
    code.append("    for file in %s:" % input_files_name)
    code.append('        %s.append("-e=" + file.path)' % input_paths_name)
    code.append("    %s = ctx.actions.declare_file(%s)" % (input_paths_file_name, input_paths_path_name))
    code.append("    ctx.actions.write(")
    code.append("        output = %s," % input_paths_file_name)
    code.append('        content = "\\n".join(%s),' % input_paths_name)
    code.append("    )")
    return code

placeholder_config = {
    "file": struct(
        attr = lambda name, data_paths, output_type: _build_file_attr(name, data_paths, output_type),
        var_decl = lambda function_name, name, output_type: ["    %s = ctx.file.%s" % (name, name)],
        action_input = lambda name: ["    action_inputs.append(%s)" % name],
        arg = lambda name, _: name,
    ),
    "string": struct(
        attr = lambda name, *_: [
            '        "%s": attr.string(' % name,
            "            mandatory = True,",
            "        ),",
        ],
        var_decl = lambda function_name, name, output_type: ["    %s = ctx.attr.%s" % (name, name)],
        action_input = None,
        arg = lambda name, _: name,
    ),
    "jar_list": struct(
        attr = lambda name, *_: [
            '        "%s": attr.label_list(' % name,
            "            mandatory = True,",
            "            providers = [JavaInfo],",
            "        ),",
        ],
        var_decl = _generate_jar_list_args_code,
        action_input = lambda name: [
            "    action_inputs += input_%s_files" % name,
            "    action_inputs += [input_%s_paths_path_file]" % name,
        ],
        arg = lambda name, _: "input_%s_paths_path_file.path" % name,
    ),
    "output": struct(
        attr = None,
        var_decl = None,
        action_input = None,
        arg = lambda name, _: "output_file.path",
    ),
    "log": struct(
        attr = None,
        var_decl = None,
        action_input = None,
        arg = lambda name, _: "%s_file.path" % name,
    ),
}

# ── step_handlers ──────────────────────────────────────────────────────────────

def _build_patch_step(task_def, ctx):
    patches = ctx.config_data["data"]["patches"]
    if type(patches) == type(""):
        task_def.append('    prefix = "%s",' % patches)
        task_def.append('    patches = "//:neoform",')
    elif type(patches) == type({}):
        task_def.append('    prefix = "%s",' % patches[ctx.side_name])
        task_def.append('    patches = "//:neoform",')
    else:
        fail("Bad patch type: %s" % type(patches))

def _build_strip_step(task_def, ctx):
    name = ctx.step.get("name", "strip")
    if ctx.spec == 1:
        task_def.append('    mappings = "//:mappings",')
    elif name == "stripClient":
        task_def.append("    generate_manifest = True,")
        task_def.append('    dist_id = "client",')
        task_def.append('    other_dist_id = "server",')
        if "strip" in ctx.config_data["steps"]["server"]:
            task_def.append('    other_dist_jar = "//tasks/server_extract_server",')
        else:
            task_def.append('    other_dist_jar = "%s",' % ctx.rctx.attr.server_jar)
        task_def.append('    mappings = "//tasks/client_merge_mappings",')
    elif name == "stripServer":
        task_def.append("    generate_manifest = True,")
        task_def.append('    dist_id = "server",')
        task_def.append('    other_dist_id = "client",')
        task_def.append('    other_dist_jar = "%s",' % ctx.rctx.attr.client_jar)
        task_def.append('    mappings = "//tasks/server_merge_mappings",')

    if ctx.spec != 1:
        deny_patterns = getattr(ctx.rctx.attr, "strip_deny_patterns", [])
        if deny_patterns:
            all_patterns = ['"META-INF/.*"'] + ['"%s"' % p for p in deny_patterns]
            task_def.append("    deny_patterns = [%s]," % ", ".join(all_patterns))

step_handlers = {
    "strip": struct(
        load_line = lambda spec: (
            'load("@//repo/neoform/rule:strip_mapped_classes.bzl", strip = "strip_mapped_classes")' if spec == 1 else 'load("@//repo/neoform/rule:split_resources.bzl", strip = "split_resources")'
        ),
        build = _build_strip_step,
    ),
    "inject": struct(
        load_line = lambda _spec: 'load("@//repo/neoform/rule:inject_zip_content.bzl", inject = "inject_zip_content")',
        build = lambda task_def, ctx: task_def.append('    deps = ["//:inject"],'),
    ),
    "patch": struct(
        load_line = lambda _spec: 'load("@//repo/neoform/rule:patch_zip_content.bzl", patch = "patch_zip_content")',
        build = _build_patch_step,
    ),
}

# ── output_placeholder_map ─────────────────────────────────────────────────────

def _resolve_list_libraries(ctx):
    sided_libraries = ['"@%s//jar"' % _convert_maven_coordinate_to_repo(ctx.version_info.repository_prefix, library) for library in ctx.config_data["libraries"][ctx.side_name]]
    sided_libraries_str = ", ".join(sided_libraries)
    if ctx.side_name == "client":
        return '    %s = ["%s", %s],' % (ctx.item_key, ctx.rctx.attr.client_libraries, sided_libraries_str)
    elif ctx.side_name == "server":
        return "    %s = [%s]," % (ctx.item_key, sided_libraries_str)
    elif ctx.side_name == "joined":
        return '    %s = ["%s", %s],' % (ctx.item_key, ctx.rctx.attr.client_libraries, sided_libraries_str)
    else:
        fail("Unsupported side when listing libraries: %s" % ctx.side_name)

output_placeholder_map = {
    "downloadClient": lambda ctx: '    %s = "%s",' % (ctx.item_key, ctx.rctx.attr.client_jar),
    "downloadServer": lambda ctx: '    %s = "%s",' % (ctx.item_key, ctx.rctx.attr.server_jar),
    "downloadClientMappings": lambda ctx: '    %s = "%s",' % (ctx.item_key, ctx.rctx.attr.client_mapping),
    "downloadServerMappings": lambda ctx: '    %s = "%s",' % (ctx.item_key, ctx.rctx.attr.server_mapping),
    "listLibraries": _resolve_list_libraries,
}
