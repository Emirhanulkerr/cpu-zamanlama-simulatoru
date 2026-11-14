#!/usr/bin/env bash
# Algoritma testlerini derler ve çalıştırır; bir kontrol başarısız olursa çıkış kodu 1 olur.
set -euo pipefail
cd "$(dirname "$0")"

./build.sh > /dev/null
rm -rf build/test-classes
mkdir -p build/test-classes

find test -name '*.java' > build/test-sources.txt
javac --release 17 -encoding UTF-8 -Xlint:all,-serial -cp build/classes -d build/test-classes @build/test-sources.txt
java -cp build/classes:build/test-classes scheduler.SchedulerTest
