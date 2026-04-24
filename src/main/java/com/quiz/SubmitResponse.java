package com.quiz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from POST /quiz/submit
 *
 * Example:
 * {
 *   "isCorrect": true,
 *   "isIdempotent": true,
 *   "submittedTotal": 220,
 *   "expectedTotal": 220,
 *   "message": "Correct!"
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitResponse {

    @JsonProperty("isCorrect")
    private boolean correct;

    @JsonProperty("isIdempotent")
    private boolean idempotent;

    @JsonProperty("submittedTotal")
    private int submittedTotal;

    @JsonProperty("expectedTotal")
    private int expectedTotal;

    @JsonProperty("message")
    private String message;

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }

    public boolean isIdempotent() { return idempotent; }
    public void setIdempotent(boolean idempotent) { this.idempotent = idempotent; }

    public int getSubmittedTotal() { return submittedTotal; }
    public void setSubmittedTotal(int submittedTotal) { this.submittedTotal = submittedTotal; }

    public int getExpectedTotal() { return expectedTotal; }
    public void setExpectedTotal(int expectedTotal) { this.expectedTotal = expectedTotal; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "SubmitResponse{correct=" + correct + ", idempotent=" + idempotent +
               ", submittedTotal=" + submittedTotal + ", expectedTotal=" + expectedTotal +
               ", message='" + message + "'}";
    }
}
