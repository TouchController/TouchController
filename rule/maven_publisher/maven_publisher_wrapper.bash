#!/usr/bin/env bash

# --- begin runfiles.bash initialization v3 ---
set -uo pipefail; set +e; f=bazel_tools/tools/bash/runfiles/runfiles.bash
source "${RUNFILES_DIR:-/dev/null}/$f" 2>/dev/null || \
  source "$(grep -sm1 "^$f " "${RUNFILES_MANIFEST_FILE:-/dev/null}" | cut -f2- -d' ')" 2>/dev/null || \
  source "$0.runfiles/$f" 2>/dev/null || \
  source "$(grep -sm1 "^$f " "$0.runfiles_manifest" | cut -f2- -d' ')" 2>/dev/null || \
  source "$(grep -sm1 "^$f " "$0.exe.runfiles_manifest" | cut -f2- -d' ')" 2>/dev/null || \
  { echo>&2 "ERROR: cannot find $f"; exit 1; }; f=; set -e
# --- end runfiles.bash initialization v3 ---

workspace_name='{WORKSPACE_NAME}'
exec_path="$(rlocation "$workspace_name"/'{EXEC_PATH}')"
pom_path="$(rlocation "$workspace_name"/'{POM_PATH}')"

artifact_ids=({ARTIFACT_IDS})
artifact_paths=({ARTIFACT_PATHS})
artifacts=()

for i in "${!artifact_ids[@]}"; do
    artifacts+=("--artifact=${artifact_ids[$i]}=$(rlocation "$workspace_name"/"${artifact_paths[$i]}")")
done

args=("--pom" "$pom_path" "--groupId" "{GROUP_ID}" "--artifactId" "{ARTIFACT_ID}" "--version" "{VERSION}" "${artifacts[@]}")

JAVA_RUNFILES="$(realpath ..)" "$exec_path" "${args[@]}"
