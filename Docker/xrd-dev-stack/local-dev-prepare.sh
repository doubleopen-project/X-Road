#!/bin/bash

set -e # Exit immediately if a command exits with a non-zero status.

errorExit() {
  echo "$(tput setaf 5)*** $*(tput sgr0)" 1>&2
  exit 1
}

warn() {
  echo "$(tput setaf 3)*** $*$(tput sgr0)"
}

# Ensure XROAD_HOME is set and not empty
if [ -z "$XROAD_HOME" ]; then
  errorExit "XROAD_HOME is not set. Exiting."
fi

TARGET_PACKAGE_SOURCE=internal
GRADLE_BUILD=1
PACKAGES_LOCAL_PATH="$XROAD_HOME"/src/packages/build/ubuntu24.04

for i in "$@"; do
  case "$i" in
  "--skip-gradle-build")
    GRADLE_BUILD=0
    ;;
  esac
done

if [[ $# -eq 0 ]]; then
  echo "Available args:"
  echo "--skip-gradle-build: Skip gradle build and use existing packages"
  echo "--skip-tests: Skip gradle build and use existing packages"
fi

if [[ "$GRADLE_BUILD" -eq 1 ]]; then
  echo "Building & packaging X-Road.."
  cd "$XROAD_HOME"/src/ && ./build_packages.sh "$@"
fi

if [ ! -d "$PACKAGES_LOCAL_PATH" ] || [ ! "$(ls -A "$PACKAGES_LOCAL_PATH")" ]; then
  errorExit "Cannot find packages in $PACKAGES_LOCAL_PATH to build docker image with. Exiting..."
fi

echo "Building xrd-centralserver-dev image.."
cd "$XROAD_HOME"/Docker/centralserver
./init_context.sh
mkdir -p build/packages
cp "$PACKAGES_LOCAL_PATH"/* build/packages/
docker build --build-arg PACKAGE_SOURCE=$TARGET_PACKAGE_SOURCE -t xrd-centralserver-dev .

echo "Building xrd-securityserver-dev image.."
cd "$XROAD_HOME"/Docker/securityserver
./init_context.sh
mkdir -p build/packages
cp "$PACKAGES_LOCAL_PATH"/* build/packages/
docker build --build-arg PACKAGE_SOURCE=$TARGET_PACKAGE_SOURCE -t xrd-securityserver-dev .

echo "Building xrd-testca image.."
cd "$XROAD_HOME"/Docker/testca
./init_context.sh
docker build -t xrd-testca .