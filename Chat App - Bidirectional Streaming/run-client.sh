#!/bin/bash

# Script to run the gRPC Chat Client GUI

cd "$(dirname "$0")"

# Run the client using gradle task
./gradlew runClient --quiet 2>/dev/null &

# Keep the script running
wait
