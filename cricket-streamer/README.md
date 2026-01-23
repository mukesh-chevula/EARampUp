# Cricket Score Streaming Server (gRPC Server Streaming)

Small Spring Boot 4 + gRPC server-streaming service that simulates a six-over cricket innings and streams every ball to the client.

## How it works
- API: server-streaming RPC `ScoreService/StreamScore` defined in [src/main/proto/score.proto](src/main/proto/score.proto). Clients send an empty request and receive a stream of `ScoreUpdate` messages for two innings.
- Service: gRPC implementation lives in [src/main/java/com/example/grpc/ScoreServiceImpl.java](src/main/java/com/example/grpc/ScoreServiceImpl.java); it plays two innings, emits ball-by-ball updates, and signals the end of each innings.
- Simulation: scoring logic is randomized in [src/main/java/com/example/engine/CricketEngine.java](src/main/java/com/example/engine/CricketEngine.java) and limits to 6 overs × 6 balls with up to 10 wickets.
- Bootstrapping: Spring Boot entry point is [src/main/java/com/example/cricketstreamer/CricketStreamerApplication.java](src/main/java/com/example/cricketstreamer/CricketStreamerApplication.java). A simple gRPC server is configured in [src/main/java/com/example/config/GrpcServerConfig.java](src/main/java/com/example/config/GrpcServerConfig.java) and defaults to port 9091.
- Config: main properties live in [src/main/resources/application.properties](src/main/resources/application.properties).

## Prerequisites
- JDK 21+
- No global Gradle install required (Gradle wrapper included)

## Run locally
From the project root:
```bash
./gradlew bootRun
 # or 
./gradlew clean build -x test
java -jar build/libs/cricket-streamer-0.0.1-SNAPSHOT.jar
```

## Test with grpcurl
Requires `grpcurl` (install via `brew install grpcurl` on macOS):
```bash
grpcurl -plaintext -d '{}' -import-path src/main/proto -proto score.proto localhost:9090 ScoreService/StreamScore
```
Example streamed messages (truncated):
```json
{
	"innings": 1,
	"over": 1,
	"ball": 1,
	"runs": 4,
	"total": 4,
	"wickets": 0,
	"comment": "FOUR!",
	"status": "PLAY"
}
{
	"innings": 1,
	"over": 0,
	"ball": 0,
	"total": 52,
	"wickets": 4,
	"comment": "End of innings 1: 52/4. Target: 53",
	"status": "INNINGS_END"
}
```

## Message schema
`ScoreUpdate` fields:
- `innings`: 1 or 2
- `target`: target score for innings 2 (0 during innings 1)
- `over`, `ball`: ball position (1-based); an `over` of 0 marks the innings-end summary
- `runs`, `total`, `wickets`: per-ball result and running totals
- `comment`: human-readable play-by-play text
- `status`: `PLAY` during balls, `INNINGS_END` after each innings

## Tests
```bash
./gradlew test
```

