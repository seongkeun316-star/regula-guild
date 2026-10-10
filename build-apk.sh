#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_SDK_ROOT:?Set ANDROID_SDK_ROOT to the installed Android SDK}"
BT="$ANDROID_SDK_ROOT/build-tools/35.0.0"
JAR="$ANDROID_SDK_ROOT/platforms/android-35/android.jar"
mkdir -p build/classes build/dex
"$BT/aapt" package -f -M app/src/main/AndroidManifest.xml -S app/src/main/res -A app/src/main/assets -I "$JAR" -F build/unsigned.apk
javac -source 8 -target 8 -encoding UTF-8 -classpath "$JAR" -d build/classes app/src/main/java/kr/cau/regulaguild/MainActivity.java
jar cf build/classes.jar -C build/classes .
"$BT/d8" --min-api 26 --lib "$JAR" --output build/dex build/classes.jar
(cd build/dex && zip -q -u ../unsigned.apk classes.dex)
"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk
# Local development signing only. Preserve this key for future updates.
if [ ! -f build/development.jks ]; then
 keytool -genkeypair -keystore build/development.jks -alias regula -storepass android -keypass android -dname 'CN=Regula Guild Development' -keyalg RSA -keysize 2048 -validity 10000
fi
"$BT/apksigner" sign --ks build/development.jks --ks-key-alias regula --ks-pass pass:android --key-pass pass:android --out build/Standaria-0.1.2.apk build/aligned.apk
"$BT/apksigner" verify --verbose build/Standaria-0.1.2.apk
