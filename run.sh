#!/usr/bin/env bash
# Projeyi derler ve çalıştırır.
#   ./run.sh                  → grafik arayüz
#   ./run.sh --cli --ornek    → konsol modu (seçenekler için: ./run.sh --cli --yardim)
set -euo pipefail
cd "$(dirname "$0")"

./build.sh > /dev/null
exec java -jar dist/CpuZamanlayici.jar "$@"
