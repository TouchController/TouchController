"Modrinth API - Enhanced version with JSON subset pinning"

load("//private:bytes_util.bzl", "hex_sha512_to_sri")

def _modrinth_repository_impl(rctx):
    urls = rctx.attr.urls
    sha512 = rctx.attr.sha512
    primary_file = rctx.attr.primary_file

    def normalize_filename(filename):
        return filename.replace("-", "_").replace("+", "_").replace(":", "_").replace(".", "_")

    download_tokens = []
    build_content = [
        'load("@rules_java//java:defs.bzl", "java_import")',
        'package(default_visibility = ["//visibility:public"])',
        "",
    ]
    for filename, url in urls.items():
        download_tokens.append(rctx.download(
            url = url,
            output = filename,
            integrity = hex_sha512_to_sri(sha512[filename]),
            block = False,
        ))
        if filename.endswith(".jar"):
            build_content += [
                "java_import(",
                '    name = "%s",' % normalize_filename(filename),
                '    jars = ["%s"],' % filename,
                ")",
            ]
        else:
            build_content += [
                "alias(",
                '    name = "%s",' % normalize_filename(filename),
                '    actual = "%s",' % filename,
                ")",
            ]
    build_content += [
        "alias(",
        '    name = "primary",',
        '    actual = "%s",' % normalize_filename(primary_file),
        ")",
    ]

    rctx.file("BUILD.bazel", content = "\n".join(build_content))

    for token in download_tokens:
        token.wait()

_modrinth_repo = repository_rule(
    implementation = _modrinth_repository_impl,
    attrs = {
        "urls": attr.string_dict(doc = "URLs for files", mandatory = True),
        "sha512": attr.string_dict(doc = "SHA512 for files", mandatory = True),
        "primary_file": attr.string(doc = "Key of primary file", mandatory = True),
    },
)

def _modrinth_pin_impl(rctx):
    pin_file = rctx.attr.pin_file
    pin_path = rctx.path(pin_file)
    pin_content = {}

    for version_id in rctx.attr.version_ids:
        # Extract SHA512 hashes for this version
        version_sha512 = {}
        for key, hash_value in rctx.attr.sha512.items():
            if key.startswith(version_id + "#"):
                filename = key[len(version_id + "#"):]
                version_sha512[filename] = hash_value

        pin_content[version_id] = {
            "sha512": version_sha512,
            "primary": rctx.attr.primary_file[version_id],
        }

    rctx.file("pin_file.json", json.encode_indent(pin_content) + "\n")
    rctx.template(
        "PinGenerator.java",
        Label("@//repo/modrinth:PinGenerator.java"),
        substitutions = {
            "$PIN_SOURCE": str(rctx.path("pin_file.json")),
            "$PIN_TARGET": str(pin_path),
        },
        executable = False,
    )

    # Generate a java_binary to build PinGenerator.java under name `pin`
    rctx.file("BUILD.bazel", content = """
load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "pin",
    srcs = ["PinGenerator.java"],
    main_class = "PinGenerator",
)
""")

_modrinth_pin = repository_rule(
    implementation = _modrinth_pin_impl,
    attrs = {
        "version_ids": attr.string_list(doc = "Version ids", mandatory = True),
        "sha512": attr.string_dict(doc = "SHA512 for files", mandatory = True),
        "primary_file": attr.string_dict(doc = "Key of primary file", mandatory = True),
        "pin_file": attr.label(doc = "Pin file", mandatory = True),
    },
)

def _download_version_json(mctx, version_id, path):
    return mctx.download(
        "https://api.modrinth.com/v2/version/%s" % version_id,
        output = path,
        block = False,
    )

modrinth_version = tag_class(
    attrs = {
        "version_id": attr.string(doc = "Modrinth Version ID", mandatory = True),
        "name": attr.string(doc = "Name of generated repository", mandatory = False),
    },
)

def _extract_version_json(json):
    sha512 = {}
    urls = {}
    primary_file = None
    for file in json["files"]:
        filename = file["filename"]
        sha512[filename] = file["hashes"]["sha512"]
        urls[filename] = file["url"]
        if file["primary"]:
            primary_file = filename
    return sha512, urls, primary_file

modrinth_pin = tag_class(
    attrs = {
        "pin_file": attr.label(
            doc = "Pin file containing file hashes",
            allow_single_file = [".json"],
            mandatory = True,
        ),
    },
)

def _modrinth_impl(mctx):
    pin_file = None
    versions = {}
    for module in mctx.modules:
        for pin in module.tags.pin:
            if pin_file != None:
                fail("Multiple pins found")
            else:
                pin_file = pin.pin_file

        for version in module.tags.version:
            if version in versions:
                item = versions[version.name]
                if item.version_id != version.version_id:
                    fail("Found different version id declared for name %s" % version)
            else:
                versions[version.name] = struct(
                    version_id = version.version_id,
                )

    pin_data = json.decode(mctx.read(pin_file)) if pin_file else None
    version_items = {}
    for name, entry in versions.items():
        pinned_data = pin_data.get(entry.version_id)
        if pinned_data == None and pin_data != None:
            print("Pin file is provided, but it don't contains data for version %s. Please repin your modrinth data by running `bazel run @modrinth_pin//:pin`." % entry.version_id)
        file = "versions/%s.json" % entry.version_id
        guard = _download_version_json(mctx, entry.version_id, file)
        if pinned_data:
            version_items[entry.version_id] = {
                "name": name,
                "pinned": True,
                "file": file,
                "guard": guard,
                "sha512": pinned_data["sha512"],
                "primary_file": pinned_data["primary"],
            }
        else:
            version_items[entry.version_id] = {
                "name": name,
                "pinned": False,
                "file": file,
                "guard": guard,
            }

    for item in version_items.values():
        item["guard"].wait()
        version_json = json.decode(mctx.read(item["file"]))
        sha512, urls, primary_file = _extract_version_json(version_json)
        item["sha512"] = sha512
        item["primary_file"] = primary_file
        item["urls"] = urls
        item["pinned"] = True

    sha512_dict = {}
    primary_file = {}
    for version, item in version_items.items():
        _modrinth_repo(
            name = item["name"],
            sha512 = item["sha512"],
            urls = item["urls"],
            primary_file = item["primary_file"],
        )
        for filename, sha512 in item["sha512"].items():
            sha512_dict[version + "#" + filename] = sha512
        primary_file[version] = item["primary_file"]

    if pin_file:
        _modrinth_pin(
            name = "modrinth_pin",
            version_ids = version_items.keys(),
            sha512 = sha512_dict,
            primary_file = primary_file,
            pin_file = pin_file,
        )

modrinth = module_extension(
    implementation = _modrinth_impl,
    tag_classes = {
        "version": modrinth_version,
        "pin": modrinth_pin,
    },
)
