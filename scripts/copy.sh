#!/bin/bash
set -e

SOURCE="./src/main/java/com/rgerva/circuitworks/"
DESTINY="scripts/copy/"

if [ ! -d "$SOURCE" ]; then
    echo "Error: '$SOURCE' does not exists!"
    exit 1
fi

mkdir -p "$DESTINY"

find "$SOURCE" -type f -exec cp -v {} "$DESTINY/" \;

echo "Copied with Successful!"