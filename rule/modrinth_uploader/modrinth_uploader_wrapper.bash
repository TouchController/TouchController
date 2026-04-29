#!/usr/bin/env bash

# A wrapper script to call the Java side modrinth uploader.

# --- begin runfiles.bash initialization v3 ---
# Copy-pasted from the Bazel Bash runfiles library v3.
set -uo pipefail; set +e; f=bazel_tools/tools/bash/runfiles/runfiles.bash
source "${RUNFILES_DIR:-/dev/null}/$f" 2>/dev/null || \
 source "$(grep -sm1 "^$f " "${RUNFILES_MANIFEST_FILE:-/dev/null}" | cut -f2- -d' ')" 2>/dev/null || \
 source "$0.runfiles/$f" 2>/dev/null || \
 source "$(grep -sm1 "^$f " "$0.runfiles_manifest" | cut -f2- -d' ')" 2>/dev/null || \
 source "$(grep -sm1 "^$f " "$0.exe.runfiles_manifest" | cut -f2- -d' ')" 2>/dev/null || \
 { echo>&2 "ERROR: cannot find $f"; exit 1; }; f=; set -e
# --- end runfiles.bash initialization v3 ---

workspace_name='{WORKSPACE_NAME}'
changelog_path="$(rlocation "$workspace_name"/'{CHANGELOG_PATH}')"
file_path="$(rlocation "$workspace_name"/'{FILE_PATH}')"
exec_path="$(rlocation "$workspace_name"/'{EXEC_PATH}')"

TEMP_FILE="$(mktemp)"
trap 'rm -f "$TEMP_FILE"' EXIT
cat >"$TEMP_FILE" <<'MODRINTH_UPLOAD_ARG_FILE'
{ARGS}
MODRINTH_UPLOAD_ARG_FILE
readarray -t args <"$TEMP_FILE"
rm "$TEMP_FILE"

if [ -n "$changelog_path" ]
then
    args=("--changelog" "$changelog_path" "${args[@]}")
fi
args=("${args[@]}" "$file_path")

JAVA_RUNFILES="$(realpath ..)" "$exec_path" "${args[@]}"
