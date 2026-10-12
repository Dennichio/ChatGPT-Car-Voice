#!/usr/bin/env bash
set -euo pipefail
# Cloud-only setup: existing checkout, no worktrees, no source/dependency rewrites.
carvoice_repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
carvoice_tools=/workspace/tooling
mkdir -p "$carvoice_tools/downloads" "$carvoice_tools/android-sdk/cmdline-tools"
# ADB requires this standard directory even when other Android state is relocated.
# If a sandbox blocks it, request scoped directory creation; never repurpose HOME.
mkdir -p /home/agent/.android

if [[ ! -x "$carvoice_tools/jdk-21.0.12.1/bin/javac" ]]; then
    curl -fsSL --retry 2 https://download.oracle.com/java/21/archive/jdk-21.0.12.1_linux-x64_bin.tar.gz -o "$carvoice_tools/downloads/jdk.tar.gz"
    printf '12f870b21301b42292558a3f872ce543affa2b86cb6458591c78388c41ddb111  %s\n' "$carvoice_tools/downloads/jdk.tar.gz" | sha256sum -c -
    tar -xzf "$carvoice_tools/downloads/jdk.tar.gz" -C "$carvoice_tools"
fi

cat > "$carvoice_tools/cloud-env.sh" <<'ENV'
export JAVA_HOME=/workspace/tooling/jdk-21.0.12.1
export ANDROID_HOME=/workspace/tooling/android-sdk
export ANDROID_USER_HOME=/workspace/tooling/android-user
export ANDROID_AVD_HOME=/workspace/tooling/avds
export ANDROID_EMULATOR_HOME=/workspace/tooling/emulator-user
export GRADLE_USER_HOME=/workspace/tooling/gradle-cache
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
if [ "${CARVOICE_JAVA_CONFIGURED:-0}" != 1 ]; then
    if [ -r /etc/ssl/certs/java/cacerts ]; then
        export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:+$JAVA_TOOL_OPTIONS }-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts"
    fi
    if [ -n "${HTTPS_PROXY:-}" ]; then
        carvoice_proxy_flags="$(python3 - <<'PY'
import os, urllib.parse, re
u = urllib.parse.urlsplit(os.environ['HTTPS_PROXY'])
if u.scheme not in ('http', 'https') or not u.hostname or not re.fullmatch(r'[a-zA-Z0-9.-]+', u.hostname):
    raise SystemExit('Unsupported HTTPS proxy format; retain verification and inspect managed proxy setup.')
if u.username or u.password:
    raise SystemExit('Credential-bearing proxy needs managed Java authentication; do not save credentials in scripts.')
p = u.port or (443 if u.scheme == 'https' else 80)
print(f'-Dhttps.proxyHost={u.hostname} -Dhttps.proxyPort={p} -Dhttp.proxyHost={u.hostname} -Dhttp.proxyPort={p} -Dhttp.nonProxyHosts=localhost|127.*')
PY
)"
        export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:+$JAVA_TOOL_OPTIONS }$carvoice_proxy_flags"
        unset carvoice_proxy_flags
    fi
    export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:+$JAVA_TOOL_OPTIONS }-Djava.util.prefs.userRoot=/workspace/tooling/java-prefs -Dddmlib.getprop.timeout.sec=60"
    export CARVOICE_JAVA_CONFIGURED=1
fi
ENV
source "$carvoice_tools/cloud-env.sh"
mkdir -p "$ANDROID_USER_HOME" "$ANDROID_AVD_HOME" "$ANDROID_EMULATOR_HOME" "$carvoice_tools/java-prefs"

if [[ ! -x "$ANDROID_HOME/cmdline-tools/23.0/bin/android" ]]; then
    curl -fsSL --retry 2 https://dl.google.com/android/repository/commandlinetools-linux-16111833_latest.zip -o "$carvoice_tools/downloads/cmdline.zip"
    # Checksum published in Google's HTTPS repository metadata; do not bypass.
    printf 'e025545c62a8e64c7559119566a569fb1dec5f60  %s\n' "$carvoice_tools/downloads/cmdline.zip" | sha1sum -c -
    unzip -q "$carvoice_tools/downloads/cmdline.zip" -d "$carvoice_tools/downloads/cmdline-unpacked"
    mv "$carvoice_tools/downloads/cmdline-unpacked/cmdline-tools" "$ANDROID_HOME/cmdline-tools/23.0"
fi
carvoice_android="$ANDROID_HOME/cmdline-tools/23.0/bin/android"
# Command-line tools 23 use `android sdk install`, replacing sdkmanager.
# This documented tool installs packages and handles the SDK terms noninteractively.
if [[ ! -f "$ANDROID_HOME/platforms/android-37.0/android.jar" ]]; then
    "$carvoice_android" --sdk="$ANDROID_HOME" sdk install 'platforms;android-37.0'
fi
if [[ ! -x "$ANDROID_HOME/build-tools/36.0.0/aapt2" ]]; then
    "$carvoice_android" --sdk="$ANDROID_HOME" sdk install 'build-tools;36.0.0'
fi
if [[ ! -x "$ANDROID_HOME/platform-tools/adb" ]]; then
    "$carvoice_android" --sdk="$ANDROID_HOME" sdk install platform-tools
fi

# Retained test tooling; a running emulator must be restarted in future tasks.
if [[ ! -x "$ANDROID_HOME/emulator/emulator" ]]; then
    "$carvoice_android" --sdk="$ANDROID_HOME" sdk install emulator
fi
if [[ ! -f "$ANDROID_HOME/system-images/android-30/default/x86_64/system.img" ]]; then
    "$carvoice_android" --sdk="$ANDROID_HOME" sdk install 'system-images;android-30;default;x86_64'
fi
if [[ ! -f "$ANDROID_AVD_HOME/carvoice-api30.ini" ]]; then
    printf 'no\n' | "$ANDROID_HOME/cmdline-tools/23.0/bin/avdmanager" create avd -n carvoice-api30 -k 'system-images;android-30;default;x86_64' -p "$ANDROID_AVD_HOME/carvoice-api30"
fi
cd "$carvoice_repo_dir"
./gradlew --no-daemon :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest
