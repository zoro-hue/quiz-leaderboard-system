package com.quiz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps the JSON response from GET /quiz/messages
 *
 * Example:
 * {
 *   "regNo": "AP23110011425",
 *   "setId": "SET_1",
 *   "pollIndex": 0,
 *   "events": [
 *     { "roundId": "R1", "participant": "Alice", "score": 10 }
 *   ]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PollResponse {

    @JsonProperty("regNo")
    private String regNo;

    @JsonProperty("setId")
    private String setId;

    @JsonProperty("pollIndex")
    private int pollIndex;

    @JsonProperty("events")
    private List<QuizEvent> events;

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { this.setId = setId; }

    public int getPollIndex() { return pollIndex; }
    public void setPollIndex(int pollIndex) { this.pollIndex = pollIndex; }

    public List<QuizEvent> getEvents() { return events; }
    public void setEvents(List<QuizEvent> events) { this.events = events; }

    @Override
    public String toString() {
        return "PollResponse{regNo='" + regNo + "', setId='" + setId +
               "', pollIndex=" + pollIndex + ", events=" + events + "}";
    }
}
