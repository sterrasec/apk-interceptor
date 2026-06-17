#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat <<EOF
Usage: ./build-interceptor.sh --scheme <custom_scheme> [options]

Options:
    --scheme    (required) Custom URI scheme to intercept
    --app-id    (optional) Override applicationId (default: com.sterrasec.apkinterceptor)
    --output    (optional) Output directory (default: ./out)
    -h, --help  Show this help
EOF
    exit 1
}

SCHEME=""
APP_ID=""
OUTPUT_DIR="./out"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --scheme) [[ $# -ge 2 ]] || usage; SCHEME="$2"; shift 2 ;;
        --app-id) [[ $# -ge 2 ]] || usage; APP_ID="$2"; shift 2 ;;
        --output) [[ $# -ge 2 ]] || usage; OUTPUT_DIR="$2"; shift 2 ;;
        -h|--help) usage ;;
        *) echo "Unknown option: $1"; usage ;;
    esac
done

if [[ -z "$SCHEME" ]]; then
    echo "Error: --scheme is required"
    usage
fi

if [[ "$SCHEME" == "intercept-poc-example" ]]; then
    echo "Error: Replace the default dummy scheme with your authorized assessment scheme"
    exit 1
fi
if [[ ! "$SCHEME" =~ ^[A-Za-z][A-Za-z0-9+.-]*$ ]]; then
    echo "Error: --scheme must be a valid URI scheme"
    exit 1
fi
if [[ -n "$APP_ID" && ! "$APP_ID" =~ ^([A-Za-z][A-Za-z0-9_]*\.)+[A-Za-z][A-Za-z0-9_]*$ ]]; then
    echo "Error: --app-id must be a valid Android application ID"
    exit 1
fi

echo "Building apk-interceptor..."
echo "  Scheme: $SCHEME"
[[ -n "$APP_ID" ]] && echo "  App ID: $APP_ID"

GRADLE_ARGS=("-PinterceptScheme=$SCHEME")
[[ -n "$APP_ID" ]] && GRADLE_ARGS+=("-PappId=$APP_ID")
./gradlew assembleDebug "${GRADLE_ARGS[@]}"

mkdir -p "$OUTPUT_DIR"
APK_NAME="apk-interceptor-${SCHEME}-debug.apk"
cp app/build/outputs/apk/debug/app-debug.apk "$OUTPUT_DIR/$APK_NAME"

echo
echo "Build successful: $OUTPUT_DIR/$APK_NAME"
echo "Install: adb install $OUTPUT_DIR/$APK_NAME"
