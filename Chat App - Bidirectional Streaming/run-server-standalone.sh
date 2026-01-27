#!/bin/bash

# Run server directly with java -jar to allow terminal input
# (gradlew bootRun consumes stdin and prevents server terminal input)

echo "════════════════════════════════════════════════════════════"
echo "Building application..."
echo "════════════════════════════════════════════════════════════"

./gradlew build -x test

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo ""
echo "════════════════════════════════════════════════════════════"
echo "Starting server..."
echo "Server terminal is ready for input"
echo "Type messages and press Enter to broadcast to all clients"
echo "════════════════════════════════════════════════════════════"
echo ""

java -jar build/libs/chat-app-bidirectional-streaming-0.0.1-SNAPSHOT.jar
