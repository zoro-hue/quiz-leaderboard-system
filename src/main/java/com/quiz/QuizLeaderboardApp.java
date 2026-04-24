package com.quiz;

/**
 * Quiz Leaderboard System - Main Entry Point
 *
 * Assignment: Internship - Java Qualifier
 * Registration Number: AP23110011425
 *
 * Flow:
 *  1. Poll the validator API 10 times (poll=0 to poll=9) with 5s delay between each
 *  2. Collect all events from all poll responses
 *  3. Deduplicate events using composite key (roundId + participant)
 *  4. Aggregate total scores per participant
 *  5. Sort leaderboard by totalScore (descending)
 *  6. Submit leaderboard once
 */
public class QuizLeaderboardApp {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Quiz Leaderboard System");
        System.out.println("  Registration No: AP23110011425");
        System.out.println("========================================");
        System.out.println();

        QuizService service = new QuizService();
        service.run();
    }
}
