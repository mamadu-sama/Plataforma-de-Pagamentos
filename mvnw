#!/bin/sh

# Apache Maven Wrapper
# Based on https://github.com/apache/maven-wrapper

set -euf

BASE_DIR=$(cd "$(dirname "$0")" && pwd)
WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
PROPS="$WRAPPER_DIR/maven-wrapper.properties"
JAR="$WRAPPER_DIR/maven-wrapper.jar"

if [ ! -f "$PROPS" ]; then
  echo "Missing $PROPS" >&2
  exit 1
fi

if [ ! -f "$JAR" ]; then
  WRAPPER_URL=$(grep -E '^wrapperUrl=' "$PROPS" | cut -d= -f2-)
  if [ -z "${WRAPPER_URL}" ]; then
    echo "Missing wrapperUrl in $PROPS" >&2
    exit 1
  fi
  mkdir -p "$WRAPPER_DIR"
  echo "Downloading Maven Wrapper from $WRAPPER_URL" >&2
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$WRAPPER_URL" -o "$JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$WRAPPER_URL" -O "$JAR"
  else
    echo "Neither curl nor wget found to download Maven Wrapper" >&2
    exit 1
  fi
fi

exec java -classpath "$JAR" "-Dmaven.multiModuleProjectDirectory=$BASE_DIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
