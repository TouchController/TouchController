"""NeoForge repository rule, fetches NeoForge artifact and setup Bazel rules"""

load("@//private:maven_coordinate.bzl", _convert_maven_coordinate = "convert_maven_coordinate", _convert_maven_coordinate_to_repo = "convert_maven_coordinate_to_repo", _convert_maven_coordinate_to_url = "convert_maven_coordinate_to_url")
load("@//private:pin_file.bzl", _parse_pin_file = "parse_pin_file")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_jar")

_neoforge_repository_url = "https://maven.neoforged.net/releases"
_minecraftforge_repository_url = "https://maven.minecraftforge.net/releases"
_config_link = "%s/net/neoforged/neoforge/%s/neoforge-%s-userdev.jar"
_config_link_legacy = "%s/net/minecraftforge/forge/%s/forge-%s-userdev.jar"
_mojang_repository_url = "https://libraries.minecraft.net"

def _convert_maven_coordinate_to_url_with_repo(repository, maven_coordinate, extension = "jar"):
    # Ugly but works
    if "mojang" in maven_coordinate or "vecmath" in maven_coordinate:
        return _convert_maven_coordinate_to_url(_mojang_repository_url, maven_coordinate, extension)
    else:
        return _convert_maven_coordinate_to_url(repository, maven_coordinate, extension)

def _neoforge_repo_impl(rctx):
    version_name = rctx.attr.version
    version_userdev_sha256 = rctx.attr.userdev_sha256
    version_universal_sha256 = rctx.attr.universal_sha256
    version_sources_sha256 = rctx.attr.sources_sha256
    version_legacy = rctx.attr.legacy
    version_java_target = rctx.attr.java_target

    neoforge_userdev_zip = "neoforge.zip"
    neoforge_universal_zip = "neoforge_unversal.jar"
    neoforge_sources_zip = "neoforge_sources.srcjar"
    output_prefix = "neoforge/%s" % version_name

    repository_url = _minecraftforge_repository_url if version_legacy else _neoforge_repository_url
    repository_prefix = "forge" if version_legacy else "neoforge"
    config_link = _config_link_legacy if version_legacy else _config_link

    rctx.report_progress("Downloading NeoForge JAR %s" % version_name)
    rctx.download(
        url = config_link % (repository_url, version_name, version_name),
        sha256 = version_userdev_sha256,
        output = neoforge_userdev_zip,
    )
    rctx.extract(
        archive = neoforge_userdev_zip,
        output = output_prefix,
    )

    config_data = json.decode(rctx.read("%s/config.json" % output_prefix))
    download_tokens = [
        rctx.download(
            url = _convert_maven_coordinate_to_url(repository_url, config_data["sources"]),
            sha256 = version_sources_sha256,
            output = neoforge_sources_zip,
            block = False,
        ),
        rctx.download(
            url = _convert_maven_coordinate_to_url(repository_url, config_data["universal"]),
            sha256 = version_universal_sha256,
            output = neoforge_universal_zip,
            block = False,
        ),
    ]
    for token in download_tokens:
        token.wait()

    neoforge_libraries = {}
    for library in config_data["libraries"]:
        label = '"@%s//jar"' % _convert_maven_coordinate_to_repo(repository_prefix, library)
        if library not in neoforge_libraries:
            neoforge_libraries[library] = label

    access_transformers = config_data["ats"]
    if type(access_transformers) == type([]):
        access_transformers = access_transformers[0]

    build_file_contents = [
        'package(default_visibility = ["//visibility:public"])',
        'load("@//repo/neoforge/rule:java_source_transform.bzl", "java_source_transform")',
        'load("@//repo/neoforge/rule:remove_manifest.bzl", "remove_manifest")',
        'load("@//repo/neoforge/rule:java_merge.bzl", "java_merge")',
        'load("@//repo/neoform/rule:patch_zip_content.bzl", "patch_zip_content")',
        'load("@//repo/neoform/rule:import_source_info.bzl", "import_source_info")',
        'load("@//repo/neoform/rule:inject_zip_content.bzl", "inject_zip_content")',
        'load("@//repo/neoform/rule:jar_import.bzl", "jar_import")',
        'load("@//repo/neoform/rule:split_resources.bzl", "strip_resources_file")',
        'load("@rules_java//java:defs.bzl", "java_library", "java_import")',
        "",
        "alias(",
        '    name = "neoforge_userdev",',
        '    actual = "%s",' % neoforge_userdev_zip,
        ")",
        "",
        "java_import(",
        '    name = "neoforge_universal",',
        '    jars = ["%s"],' % neoforge_universal_zip,
        '    srcjar = "%s",' % neoforge_sources_zip,
        ")",
        "",
        "alias(",
        '    name = "neoforge_sources",',
        '    actual = "%s",' % neoforge_sources_zip,
        ")",
        "",
        "patch_zip_content(",
        '    name = "add_neoforge_patches",',
        '    prefix = "%s",' % config_data["patches"],
        '    patches = ":neoforge_userdev",',
        '    input = "%s",' % rctx.attr.joined_patched_sources,
        ")",
        "",
        "java_source_transform(",
        '    name = "transform_sources",',
        '    input = ":add_neoforge_patches",',
        '    access_transformers = glob(["%s/%s*"]),' % (output_prefix, access_transformers),
        ")",
        "",
        "import_source_info(",
        '    name = "decompile_libraries",',
        '    deps = [":transform_sources"],',
        ")",
        "",
        "java_library(",
        '    name = "recompile_with_manifest",',
        '    srcs = [":transform_sources"],',
        "    deps = [",
        '        ":decompile_libraries",',
        '        ":neoforge_universal",',
        "        %s" % ", \n        ".join(neoforge_libraries.values()),
        "    ],",
        '    javacopts = ["-XepDisableAllChecks", "-nowarn", "-g", "-proc:none", "-implicit:none", "--release", "%s"],' % version_java_target,
        ")",
        "",
        "remove_manifest(",
        '    name = "recompile",',
        '    src = ":recompile_with_manifest",',
        ")",
        "",
        "java_import(",
        '    name = "recompile_with_deps",',
        '    jars = [":recompile"],',
        '    srcjar = ":transform_sources",',
        ")",
        "",
        "inject_zip_content(",
        '    name = "sources_with_neoforge",',
        '    input = ":transform_sources",',
        '    extension = "jar",',
        '    deps = [":neoforge_sources"],',
        ")",
        "",
        "inject_zip_content(",
        '    name = "compiled_with_neoforge",',
        '    input = ":recompile",',
        '    deps = [":neoforge_universal"],',
        ")",
        "",
        "strip_resources_file(",
        '    name = "strip_resources",',
        '    src = "%s",' % rctx.attr.joined_strip_client,
        ")",
        "",
        "jar_import(",
        '    name = "neoforge",',
        '    jar = ":compiled_with_neoforge",',
        '    srcjar = ":sources_with_neoforge",',
        '    runtime_deps = [":strip_resources"],',
        ")",
        "",
        "java_merge(",
        '    name = "neoforge_deps",',
        "    deps = [",
        '        ":decompile_libraries",',
        "        %s" % ", \n        ".join(neoforge_libraries.values()),
        "    ],",
        ")",
    ]

    for library, label in neoforge_libraries.items():
        items = library.split(":")
        build_file_contents += [
            "alias(",
            '    name = "%s",' % _convert_maven_coordinate("%s:%s" % (items[0], items[1])),
            "    actual = %s," % label,
            ")",
        ]

    rctx.file("BUILD.bazel", "\n".join(build_file_contents))

_neoforge_repo = repository_rule(
    implementation = _neoforge_repo_impl,
    attrs = {
        "java_target": attr.int(
            doc = "Java target",
            mandatory = True,
        ),
        "version": attr.string(
            doc = "Version of NeoForge",
            mandatory = True,
        ),
        "legacy": attr.bool(
            doc = "Use legacy minecraftforge(lexforge) instead neoforged repository.",
            mandatory = False,
            default = False,
        ),
        "userdev_sha256": attr.string(
            doc = "SHA-256 of the NeoForge userdev JAR file",
            mandatory = True,
        ),
        "universal_sha256": attr.string(
            doc = "SHA-256 of the NeoForge universal JAR file",
            mandatory = True,
        ),
        "sources_sha256": attr.string(
            doc = "SHA-256 of the NeoForge sources JAR file",
            mandatory = True,
        ),
        "joined_patched_sources": attr.label(
            doc = "Joined patched sources, usually come from NeoForm",
            allow_single_file = [".jar"],
            mandatory = True,
        ),
        "joined_strip_client": attr.label(
            doc = "Strip client task from NeoForm pipeline, providing SplitResourceInfo",
            mandatory = True,
        ),
    },
)

def _neoforge_pin_impl(rctx):
    url_lines = ['"%s"' % url for url in rctx.attr.urls]
    pin_target = str(rctx.path(rctx.attr.pin_file)) if rctx.attr.pin_file else "neoforge_pin.txt"
    rctx.template("PinGenerator.java", rctx.attr._pinner_source, {
        "/*INJECT HERE*/": ", ".join(url_lines),
        "$PIN_TARGET": pin_target,
    })

    build_bazel_contents = [
        'load("@rules_java//java:defs.bzl", "java_binary")',
        'package(default_visibility = ["//visibility:public"])',
        "",
        "java_binary(",
        '    name = "pin",',
        '    srcs = ["PinGenerator.java"],',
        '    main_class = "PinGenerator",',
        ")",
    ]
    rctx.file("BUILD.bazel", "\n".join(build_bazel_contents))

neoforge_pin = repository_rule(
    implementation = _neoforge_pin_impl,
    attrs = {
        "urls": attr.string_list(
            doc = "List of URLs to pin",
        ),
        "pin_file": attr.label(
            doc = "Pin file output path",
            allow_single_file = True,
            mandatory = False,
        ),
        "_pinner_source": attr.label(
            allow_single_file = [".java"],
            default = "@//repo/neoform/pin_generator:PinGenerator.java",
        ),
    },
)

version = tag_class(
    attrs = {
        "java_target": attr.int(
            doc = "Java target",
            mandatory = True,
        ),
        "version": attr.string(
            doc = "Version of NeoForge",
            mandatory = True,
        ),
        "legacy": attr.bool(
            doc = "Use legacy minecraftforge(lexforge) instead neoforged repository.",
            mandatory = False,
            default = False,
        ),
        "userdev_sha256": attr.string(
            doc = "SHA-256 of the NeoForge userdev JAR file",
            mandatory = True,
        ),
        "universal_sha256": attr.string(
            doc = "SHA-256 of the NeoForge universal JAR file",
            mandatory = True,
        ),
        "sources_sha256": attr.string(
            doc = "SHA-256 of the NeoForge sources JAR file",
            mandatory = True,
        ),
        "joined_patched_sources": attr.label(
            doc = "Joined patched sources, usually come from NeoForm",
            allow_single_file = [".jar"],
            mandatory = True,
        ),
        "joined_strip_client": attr.label(
            doc = "Strip client task from NeoForm pipeline, providing SplitResourceInfo",
            mandatory = True,
        ),
    },
)

pin = tag_class(
    attrs = {
        "pin_file": attr.label(
            doc = "Pin file",
            allow_single_file = [".txt"],
            mandatory = False,
        ),
    },
)

def _neoforge_impl(mctx):
    versions = {}
    pin_file = None
    for module in mctx.modules:
        for pin in module.tags.pin:
            if pin_file != None:
                fail("Multiple pins found")
            else:
                pin_file = pin.pin_file
        for version in module.tags.version:
            if version in versions:
                if versions[version.version].userdev_sha256 != version.userdev_sha256:
                    fail("NeoForm version %s already exists with a different userdev SHA-256" % version.version)
                elif versions[version.version].legacy != version.legacy:
                    fail("NeoForm version %s already exists with a different legacy flag" % version.version)
                elif versions[version.version].universal_sha256 != version.universal_sha256:
                    fail("NeoForm version %s already exists with a different universal SHA-256" % version.version)
                elif versions[version.version].sources_sha256 != version.sources_sha256:
                    fail("NeoForm version %s already exists with a different sources SHA-256" % version.version)
                elif versions[version.version].joined_patched_sources != version.joined_patched_sources:
                    fail("NeoForm version %s already exists with a different joined patched sources" % version.version)
                elif versions[version.version].joined_strip_client != version.joined_strip_client:
                    fail("NeoForm version %s already exists with a different joined strip client" % version.version)
                elif versions[version.version].java_target != version.java_target:
                    fail("NeoForm version %s already exists with a different java target" % version.version)
            else:
                versions[version.version] = {
                    "version": version.version,
                    "java_target": version.java_target,
                    "legacy": version.legacy,
                    "userdev_sha256": version.userdev_sha256,
                    "universal_sha256": version.universal_sha256,
                    "sources_sha256": version.sources_sha256,
                    "joined_patched_sources": version.joined_patched_sources,
                    "joined_strip_client": version.joined_strip_client,
                }
    versions = versions.values()

    libraries = []

    def append_library(coordinate, legacy = False):
        item = {
            "coordinate": coordinate,
            "legacy": legacy,
        }
        if item not in libraries:
            libraries.append(item)

    for version in versions:
        version_name = version["version"]
        version_userdev_sha256 = version["userdev_sha256"]
        version_legacy = version["legacy"]
        version_java_target = version["java_target"]
        output_prefix = "neoforge/%s" % version_name

        config_link = _config_link_legacy if version_legacy else _config_link
        repository_url = _minecraftforge_repository_url if version_legacy else _neoforge_repository_url
        repository_prefix = "forge" if version_legacy else "neoforge"

        mctx.report_progress("Downloading NeoForm JAR %s" % version_name)
        mctx.download_and_extract(
            url = config_link % (repository_url, version_name, version_name),
            type = "zip",
            sha256 = version_userdev_sha256,
            output = output_prefix,
        )

        repo_name = "%s_%s" % (repository_prefix, _convert_maven_coordinate(version_name))
        _neoforge_repo(
            name = repo_name,
            version = version_name,
            java_target = version_java_target,
            legacy = version_legacy,
            userdev_sha256 = version_userdev_sha256,
            universal_sha256 = version["universal_sha256"],
            sources_sha256 = version["sources_sha256"],
            joined_patched_sources = version["joined_patched_sources"],
            joined_strip_client = version["joined_strip_client"],
        )

        config_data = json.decode(mctx.read("%s/config.json" % output_prefix))
        for library in config_data["libraries"]:
            append_library(library, version_legacy)

    pin_content = {}
    if pin_file != None:
        pin_content = _parse_pin_file(mctx.read(pin_file))
    for library in libraries:
        coordinate = library["coordinate"]
        legacy = library["legacy"]
        repository_url = _minecraftforge_repository_url if legacy else _neoforge_repository_url
        repository_prefix = "forge" if legacy else "neoforge"
        http_jar(
            name = _convert_maven_coordinate_to_repo(repository_prefix, coordinate),
            url = _convert_maven_coordinate_to_url_with_repo(repository_url, coordinate),
            sha256 = pin_content.get(_convert_maven_coordinate_to_url(repository_url, coordinate), None),
        )

    neoforge_pin(
        name = "neoforge_pin",
        urls = [
            _convert_maven_coordinate_to_url_with_repo(
                _minecraftforge_repository_url if library["legacy"] else _neoforge_repository_url,
                library["coordinate"],
            )
            for library in libraries
        ],
        pin_file = pin_file,
    )

neoforge = module_extension(
    implementation = _neoforge_impl,
    tag_classes = {
        "version": version,
        "pin": pin,
    },
)
