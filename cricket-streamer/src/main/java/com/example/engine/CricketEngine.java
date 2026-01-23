package com.example.engine;

import java.util.Random;

public class CricketEngine {
    private final Random random = new Random();
    private int total = 0;
    private int wickets = 0;

    public static final int OVERS = 6;
    public static final int BALLS = 6;

    public record Ball(int over, int ball, int runs, int total, int wickets, String comment) {}

    public Ball next(int over, int ball) {
        int p = random.nextInt(100);
        int runs = 0;
        String comment;

        if (p < 3) {
            wickets++;
            comment = "WICKET!";
        } else if (p < 38) {
            comment = "Dot ball";
        } else if (p < 63) {
            runs = 1; comment = "1 run";
        } else if (p < 78) {
            runs = 2; comment = "2 runs";
        } else if (p < 85) {
            runs = 3; comment = "3 runs";
        } else if (p < 93) {
            runs = 4; comment = "FOUR!";
        } else {
            runs = 6; comment = "SIX!";
        }

        total += runs;
        return new Ball(over, ball, runs, total, wickets, comment);
    }
}
