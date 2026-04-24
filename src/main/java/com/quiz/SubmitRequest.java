package com.quiz;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request payload for POST /quiz/submit
 *
 * Example:
 * {
 *   "regNo": "AP23110011425",
 *   "leaderboard": [
 *     { "participant": "Alice", "totalScore": 100 },
 *     { "participant": "Bob", "totalScore": 120 }
 *   ]
 * }
 */
public class SubmitRequest {

    @JsonProperty("regNo")
    private String regNo;

    @JsonProperty("leaderboard")
    private List<LeaderboardEntry> leaderboard;

    public SubmitRequest() {}

    public SubmitRequest(String regNo, List<LeaderboardEntry> leaderboard) {
        this.regNo = regNo;
        this.leaderboard = leaderboard;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public List<LeaderboardEntry> getLeaderboard() { return leaderboard; }
    public void setLeaderboard(List<LeaderboardEntry> leaderboard) { this.leaderboard = leaderboard; }
}
