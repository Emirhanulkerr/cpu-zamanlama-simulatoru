#!/usr/bin/env bash
# Kaynak kodu derler ve çalıştırılabilir JAR dosyasını üretir: dist/CpuZamanlayici.jar
set -euo pipefail
cd "$(dirname "$0")"

rm -rf build/classes
mkdir -p build/classes dist

find src -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -Xlint:all,-serial -d build/classes @build/sources.txt
jar --create --file dist/CpuZamanlayici.jar --main-class scheduler.Main -C build/classes .

echo "Derleme tamamlandı: dist/CpuZamanlayici.jar"
