#!/usr/bin/env bash
set -euo pipefail
source /workspace/tooling/cloud-env.sh
carvoice_serial=emulator-5554
carvoice_tools=/workspace/tooling
mkdir -p "$ANDROID_EMULATOR_HOME" "$carvoice_tools/logs"
if [[ ! -f "$ANDROID_AVD_HOME/carvoice-api30.ini" ]]; then
    echo "Test-AVD ontbreekt; voer scripts/cloud-setup.sh eerst uit." >&2
    exit 1
fi

if ! adb devices | rg -q "^${carvoice_serial}[[:space:]]"; then
    # Cloud has no /dev/kvm. AOSP API30 avoids unnecessary Google services; TCG/software GPU is intentional.
    nohup "$ANDROID_HOME/emulator/emulator" -avd carvoice-api30 -port 5554 \
        -no-window -no-audio -no-boot-anim -no-snapshot -gpu swiftshader \
        -accel off -memory 2048 -cores 2 >"$carvoice_tools/logs/emulator.log" 2>&1 </dev/null &
    echo "Emulator gestart; wacht op volledige Android-opstart."
else
    echo "Bestaande testemulator gevonden; controleer Android-opstart."
fi
for ((carvoice_attempt=1; carvoice_attempt<=90; carvoice_attempt++)); do
    if [[ "$(timeout 8 adb -s "$carvoice_serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)" == 1 ]]; then
        carvoice_sdk="$(adb -s "$carvoice_serial" shell getprop ro.build.version.sdk | tr -d '\r')"
        if [[ "$carvoice_sdk" != 30 ]]; then
            echo "Poort 5554 bevat een andere Android-versie ($carvoice_sdk); stop geen onbekende emulator automatisch." >&2
            exit 1
        fi
        adb -s "$carvoice_serial" shell input keyevent 82
        adb -s "$carvoice_serial" shell settings put global window_animation_scale 0
        adb -s "$carvoice_serial" shell settings put global transition_animation_scale 0
        adb -s "$carvoice_serial" shell settings put global animator_duration_scale 0
        echo "Android API30 is gereed op $carvoice_serial."
        exit 0
    fi
    if ((carvoice_attempt % 6 == 0)); then echo "Android wordt nog gestart (controle $carvoice_attempt/90)."; fi
    sleep 5
done
echo "Android is niet tijdig gestart. Inspecteer emulatorlog en ADB; dit is geen geslaagde readiness-check." >&2
exit 1
