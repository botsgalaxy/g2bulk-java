#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
target=${1:-all}
rm -rf build
mkdir -p build/main build/test
javac --release 11 -Xlint:all -Werror -d build/main $(find src/main/java -name '*.java')
javac --release 11 -Xlint:all,-serial -cp build/main -d build/test $(find src/test/java -name '*.java')
cp=build/main:build/test
case "$target" in
  compile) ;;
  unit) java -cp "$cp" com.g2bulk.UnitTests ;;
  e2e) java -cp "$cp" -Dg2bulk.e2e.dir=../e2e com.g2bulk.E2ETests ;;
  all)
    java -cp "$cp" com.g2bulk.UnitTests
    java -cp "$cp" -Dg2bulk.e2e.dir=../e2e com.g2bulk.E2ETests
    ;;
  *) echo "usage: ./build.sh [compile|unit|e2e|all]" >&2; exit 2 ;;
esac
