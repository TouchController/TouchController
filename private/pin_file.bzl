"""Utilities for parsing and managing pin files."""

def parse_pin_file(content):
    """Parse a pin file and extract hash information.

    Args:
        content: The content of the pin file as a string.

    Returns:
        A dictionary mapping URLs to their hash values.
    """
    lines = content.splitlines()
    hashes = {}
    for line in lines:
        space_index = line.find(" ")
        url = line[:space_index]
        hash = line[space_index + 1:]
        hashes[url] = hash
    return hashes

def _pin_impl(rctx):
    entries = rctx.attr.entries
    manifest = {}
    jvm_flags_parts = []

    for index, (label, url) in enumerate(entries.items()):
        flag = "pin.file.%d" % index
        manifest[url] = flag
        jvm_flags_parts.append('"-D%s=$(rlocationpath %s)"' % (flag, label))

    rctx.file("pin_manifest.json", json.encode_indent(manifest) + "\n")

    pin_target = str(rctx.path(rctx.attr.pin_file)) if rctx.attr.pin_file else "pin.txt"
    jvm_flags_parts.insert(0, '"-Dpin.manifest=$(rlocationpath :pin_manifest.json)"')
    jvm_flags_parts.insert(0, '"-Dpin.target=%s"' % pin_target)

    entry_label_strings = ['"%s"' % l for l in entries.keys()]

    data_parts = ['"pin_manifest.json"']
    if entry_label_strings:
        data_parts.extend(entry_label_strings)

    build_bazel_contents = [
        'load("@rules_java//java:defs.bzl", "java_binary")',
        'package(default_visibility = ["//visibility:public"])',
        "",
        "java_binary(",
        '    name = "pin",',
        '    srcs = ["@//repo/neoform/pin_generator:PinGenerator.java"],',
        '    main_class = "PinGenerator",',
        "    deps = [",
        '        "@bazel_tools//tools/java/runfiles",',
        '        "@maven//:com_google_code_gson_gson",',
        "    ],",
        "    data = [",
        "        %s," % ",\n        ".join(data_parts),
        "    ],",
        "    jvm_flags = [",
        "        %s," % ",\n        ".join(jvm_flags_parts),
        "    ],",
        ")",
    ]
    rctx.file("BUILD.bazel", "\n".join(build_bazel_contents))

def pin_file():
    return repository_rule(
        implementation = _pin_impl,
        attrs = {
            "entries": attr.string_dict(
                doc = "Dict mapping label strings to URL strings",
            ),
            "pin_file": attr.label(
                doc = "Pin file output path",
                allow_single_file = True,
                mandatory = False,
            ),
        },
    )
