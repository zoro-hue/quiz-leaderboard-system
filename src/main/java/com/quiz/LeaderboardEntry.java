package com.quiz;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single entry in the final leaderboard.
 *
 * Example: { "participant": "Alice", "totalScore": 100 }
 */
public class LeaderboardEntry {

    @JsonProperty("participant")
    private String participant;

    @JsonProperty("totalScore")
    private int totalScore;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String participant, int totalScore) {
        this.participant = participant;
        this.totalScore = totalScore;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public String getParticipant() { return participant; }
    public void setParticipant(String participant) { this.participant = participant; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    @Override
    public String toString() {
        return "LeaderboardEntry{participant='" + participant + "', totalScore=" + totalScore + "}";
    }
}
