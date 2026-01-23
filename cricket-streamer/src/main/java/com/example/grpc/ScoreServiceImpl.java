package com.example.grpc;

import com.example.engine.CricketEngine;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class ScoreServiceImpl extends ScoreServiceGrpc.ScoreServiceImplBase {

    private static final int MAX_WICKETS = 10;

    @Override
    public void streamScore(StreamRequest request, StreamObserver<ScoreUpdate> responseObserver) {
        int inningsOneTotal = playInnings(1, 0, responseObserver);

        int target = inningsOneTotal + 1;
        playInnings(2, target, responseObserver);

        responseObserver.onCompleted();
    }

    private int playInnings(int inningsNumber, int target, StreamObserver<ScoreUpdate> responseObserver) {
        CricketEngine engine = new CricketEngine();
        int latestTotal = 0;
        int latestWickets = 0;

        inningsLoop:
        for (int o = 1; o <= CricketEngine.OVERS; o++) {
            for (int b = 1; b <= CricketEngine.BALLS; b++) {
                CricketEngine.Ball br = engine.next(o, b);
                latestTotal = br.total();
                latestWickets = br.wickets();

                ScoreUpdate update = ScoreUpdate.newBuilder()
                        .setInnings(inningsNumber)
                        .setTarget(target)
                        .setOver(br.over())
                        .setBall(br.ball())
                        .setRuns(br.runs())
                        .setTotal(latestTotal)
                        .setWickets(latestWickets)
                        .setComment(br.comment())
                        .setStatus("PLAY")
                        .build();

                responseObserver.onNext(update);

                if (inningsNumber == 2 && target > 0 && latestTotal >= target) {
                    break inningsLoop; // chase completed
                }
                if (latestWickets >= MAX_WICKETS) {
                    break inningsLoop; // all out
                }

                try { Thread.sleep(700); } catch (InterruptedException ignored) {}
            }
        }

        String endComment;
        if (inningsNumber == 1) {
            endComment = "End of innings 1: " + latestTotal + "/" + latestWickets + ". Target: " + (latestTotal + 1);
        } else {
            if (latestTotal >= target) {
                endComment = "Innings 2: chase complete. Won by " + (MAX_WICKETS - latestWickets) + " wickets";
            } else {
                endComment = "Innings 2: fell short. Needed " + (target - latestTotal) + " more";
            }
        }

        ScoreUpdate finalUpdate = ScoreUpdate.newBuilder()
                .setInnings(inningsNumber)
                .setTarget(target)
                .setOver(0)
                .setBall(0)
                .setRuns(0)
                .setTotal(latestTotal)
                .setWickets(latestWickets)
                .setComment(endComment)
                .setStatus("INNINGS_END")
                .build();

        responseObserver.onNext(finalUpdate);
        return latestTotal;
    }
}
