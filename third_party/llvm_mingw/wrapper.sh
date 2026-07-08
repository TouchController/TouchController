#!/bin/bash -e
export PATH="/usr/bin:/sbin:/bin:$PATH"
wrapper_path="$(dirname "$0")"

new_args=()
for arg in "$@"; do
    if [ "$arg" = "-lunwind" ]; then
        new_args+=("-l:libunwind.a")
    else
        new_args+=("$arg")
    fi
done

exec "$wrapper_path/../bin/%{REAL_TOOL}" "${new_args[@]}"
