#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p out
javac -Xlint:all -d out src/*.java
exec java -cp out Main
