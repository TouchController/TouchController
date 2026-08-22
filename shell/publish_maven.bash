LOCAL_REPO="file://$HOME/.m2/repository"
SNAPSHOT_REPO="https://maven.fifthlight.top/snapshots"
RELEASE_REPO="https://maven.fifthlight.top/releases"

usage() {
    echo "Usage: $0 [-l] [-s] [-r] [-b] [-- [extra arguments...]]"
    echo "Options:"
    echo "  -l    Publish to local Maven repository"
    echo "  -s    Publish to snapshot Maven repository"
    echo "  -r    Publish to release Maven repository"
    echo "  -b    Create signed ZIP bundle(s) for Maven Central upload"
    echo "  -h    Show this help message"
    exit 1
}

if [ "$#" -eq 0 ]; then
    usage
fi

REPO_TYPE=""
while getopts "lsrb:h" opt; do
    case ${opt} in
        l)
            if [ -n "$REPO_TYPE" ]; then
                echo "Error: Only one repository type can be specified."
                usage
            fi
            REPO_TYPE="local"
            ;;
        s)
            if [ -n "$REPO_TYPE" ]; then
                echo "Error: Only one repository type can be specified."
                usage
            fi
            REPO_TYPE="snapshot"
            ;;
        r)
            if [ -n "$REPO_TYPE" ]; then
                echo "Error: Only one repository type can be specified."
                usage
            fi
            REPO_TYPE="release"
            ;;
        b)
            if [ -n "$REPO_TYPE" ]; then
                echo "Error: Only one repository type can be specified."
                usage
            fi
            REPO_TYPE="bundle"
            BUNDLE_PATH="$OPTARG"
            ;;
        h)
            usage
            ;;
        *)
            usage
            ;;
    esac
done

case "$REPO_TYPE" in
    "local")
        TARGET_REPO="$LOCAL_REPO"
        ;;
    "snapshot")
        TARGET_REPO="$SNAPSHOT_REPO"
        ;;
    "release")
        TARGET_REPO="$RELEASE_REPO"
        ;;
    "bundle")
        ;;
    *)
        echo "Error: No repository type specified."
        usage
        ;;
esac

shift $((OPTIND - 1))

EXTRA_ARGUMENTS=( "$@" )

function publish() {
    if [ "$REPO_TYPE" = "bundle" ]; then
        BASEDIR="$PWD" bazel run "$1" -- --bundle="${2+"$2-"}$BUNDLE_PATH" --sign "${EXTRA_ARGUMENTS[@]}"
    else
        bazel run "$1" -- --repo-url="$TARGET_REPO" "${EXTRA_ARGUMENTS[@]}"
    fi
}
