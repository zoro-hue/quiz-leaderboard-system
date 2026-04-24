package com.quiz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single quiz event from an API poll response.
 *
 * Deduplication key: roundId + participant (composite)
 *
 * Example: { "roundId": "R1", "participant": "Alice", "score": 10 }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuizEvent {

    @JsonProperty("roundId")
    private String roundId;

    @JsonProperty("participant")
    private String participant;

    @JsonProperty("score")
    private int score;

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public String getRoundId() { return roundId; }
    public void setRoundId(String roundId) { this.roundId = roundId; }

    public String getParticipant() { return participant; }
    public void setParticipant(String participant) { this.participant = participant; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    /**
     * Returns the composite deduplication key for this event.
     */
    public String getDeduplicationKey() {
        return roundId + "|" + participant;
    }

    @Override
    public String toString() {
        return "QuizEvent{roundId='" + roundId + "', participant='" + participant + "', score=" + score + "}";
    }
}
