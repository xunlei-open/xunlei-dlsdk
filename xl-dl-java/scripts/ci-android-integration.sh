#!/usr/bin/env bash
set -euo pipefail

adb wait-for-device
adb shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 2; done'

abi_list="$(adb shell getprop ro.product.cpu.abilist | tr -d '\r')"
echo "emulator abi list: ${abi_list}"
if ! echo "${abi_list}" | grep -q 'arm64-v8a'; then
  echo "x86_64 emulator does not advertise arm64-v8a; ARM native libs cannot load." >&2
  exit 1
fi

./gradlew :android:integrationTest --info

python3 - <<'PY'
import glob
import sys
import xml.etree.ElementTree as ET

files = glob.glob("android/build/outputs/androidTest-results/**/TEST-*.xml", recursive=True)
if not files:
    sys.exit("No connected Android test XML found; instrumentation did not produce results.")
tree = ET.parse(files[0])
root = tree.getroot()
tests = int(float(root.attrib.get("tests", "0")))
failures = int(float(root.attrib.get("failures", "0")))
errors = int(float(root.attrib.get("errors", "0")))
skipped = int(float(root.attrib.get("skipped", "0")))
print(f"result xml={files[0]} tests={tests} failures={failures} errors={errors} skipped={skipped}")
if tests < 1 or failures or errors or skipped:
    sys.exit("Android integration test did not pass.")
cases = list(root.findall("testcase"))
if not cases:
    sys.exit("Android integration XML has no testcase entries.")
for case in cases:
    elapsed = float(case.attrib.get("time", "0"))
    name = case.attrib.get("name", "?")
    print(f"testcase {name} time={elapsed}")
    if elapsed < 1:
        sys.exit(f"{name} finished in {elapsed}s; treating as not actually executed.")
PY
