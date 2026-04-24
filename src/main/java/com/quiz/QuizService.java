package com.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Core service that orchestrates polling, deduplication, aggregation, and submission.
 */
public class QuizService {

    private static final String REG_NO = "AP23110011425";
    private static final int TOTAL_POLLS = 10;
    private static final int POLL_DELAY_MS = 5000; // 5 seconds mandatory delay

    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public QuizService() {
        this.apiClient = new ApiClient();
        this.objectMapper = new ObjectMapper();
    }

    public void run() {
        try {
            // Step 1 & 2: Poll API 10 times and collect all events
            List<QuizEvent> allEvents = pollAllRounds();

            // Step 3: Deduplicate events using (roundId + participant) as composite key
            List<QuizEvent> uniqueEvents = deduplicateEvents(allEvents);

            // Step 4: Aggregate scores per participant
            Map<String, Integer> scoreMap = aggregateScores(uniqueEvents);

            // Step 5: Generate leaderboard sorted by totalScore (descending)
            List<LeaderboardEntry> leaderboard = buildLeaderboard(scoreMap);

            // Step 6: Compute total score across all users
            int totalScore = leaderboard.stream()
                    .mapToInt(LeaderboardEntry::getTotalScore)
                    .sum();

            // Print leaderboard summary
            printLeaderboard(leaderboard, totalScore);

            // Step 7: Submit leaderboard once
            submitLeaderboard(leaderboard, totalScore);

        } catch (Exception e) {
            System.err.println("[ERROR] An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Step 1 & 2: Poll the API 10 times with 5-second delay between each poll.
     * Collects all events across all polls.
     */
    private List<QuizEvent> pollAllRounds() throws InterruptedException {
        List<QuizEvent> allEvents = new ArrayList<>();

        System.out.println("[INFO] Starting to poll API (" + TOTAL_POLLS + " polls with " + (POLL_DELAY_MS / 1000) + "s delay between each)...");
        System.out.println();

        for (int poll = 0; poll < TOTAL_POLLS; poll++) {
            System.out.printf("[POLL %d/%d] Fetching data for poll=%d...%n", poll + 1, TOTAL_POLLS, poll);

            PollResponse response = apiClient.fetchPoll(REG_NO, poll);

            if (response != null && response.getEvents() != null) {
                int eventCount = response.getEvents().size();
                System.out.printf("           Received %d event(s) from setId=%s%n", eventCount, response.getSetId());
                allEvents.addAll(response.getEvents());
            } else {
                System.out.printf("           No events received for poll=%d%n", poll);
            }

            // Wait 5 seconds between polls (not needed after the last poll)
            if (poll < TOTAL_POLLS - 1) {
                System.out.printf("           Waiting %d seconds before next poll...%n", POLL_DELAY_MS / 1000);
                Thread.sleep(POLL_DELAY_MS);
            }
        }

        System.out.println();
        System.out.println("[INFO] Total raw events collected (before dedup): " + allEvents.size());
        return allEvents;
    }

    /**
     * Step 3: Deduplicate events using composite key (roundId + "|" + participant).
     * If the same (roundId, participant) pair appears more than once, keep only the first occurrence.
     */
    private List<QuizEvent> deduplicateEvents(List<QuizEvent> allEvents) {
        Set<String> seen = new LinkedHashSet<>();
        List<QuizEvent> uniqueEvents = new ArrayList<>();

        int duplicateCount = 0;

        for (QuizEvent event : allEvents) {
            // Composite deduplication key
            String key = event.getRoundId() + "|" + event.getParticipant();

            if (seen.add(key)) {
                // New event — keep it
                uniqueEvents.add(event);
            } else {
                // Duplicate — ignore it
                duplicateCount++;
                System.out.printf("[DEDUP] Skipping duplicate: roundId=%s, participant=%s, score=%d%n",
                        event.getRoundId(), event.getParticipant(), event.getScore());
            }
        }

        System.out.println();
        System.out.println("[INFO] Duplicates removed: " + duplicateCount);
        System.out.println("[INFO] Unique events after deduplication: " + uniqueEvents.size());
        return uniqueEvents;
    }

    /**
     * Step 4: Aggregate total scores per participant from deduplicated events.
     */
    private Map<String, Integer> aggregateScores(List<QuizEvent> uniqueEvents) {
        Map<String, Integer> scoreMap = new LinkedHashMap<>();

        for (QuizEvent event : uniqueEvents) {
            scoreMap.merge(event.getParticipant(), event.getScore(), Integer::sum);
        }

        System.out.println();
        System.out.println("[INFO] Score aggregation complete. Participants: " + scoreMap.size());
        return scoreMap;
    }

    /**
     * Step 5: Build a sorted leaderboard (descending by totalScore, then alphabetically by name for ties).
     */
    private List<LeaderboardEntry> buildLeaderboard(Map<String, Integer> scoreMap) {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            leaderboard.add(new LeaderboardEntry(entry.getKey(), entry.getValue()));
        }

        // Sort by totalScore descending; on tie, sort alphabetically by participant name
        leaderboard.sort(Comparator
                .comparingInt(LeaderboardEntry::getTotalScore).reversed()
                .thenComparing(LeaderboardEntry::getParticipant));

        return leaderboard;
    }

    /**
     * Print the leaderboard to console for verification.
     */
    private void printLeaderboard(List<LeaderboardEntry> leaderboard, int totalScore) {
        System.out.println();
        System.out.println("==================== LEADERBOARD ====================");
        System.out.printf("%-5s %-20s %s%n", "Rank", "Participant", "Total Score");
        System.out.println("------------------------------------------------------");

        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardEntry entry = leaderboard.get(i);
            System.out.printf("%-5d %-20s %d%n", i + 1, entry.getParticipant(), entry.getTotalScore());
        }

        System.out.println("------------------------------------------------------");
        System.out.printf("%-5s %-20s %d%n", "", "GRAND TOTAL", totalScore);
        System.out.println("======================================================");
        System.out.println();
    }

    /**
     * Step 7: Submit the leaderboard once to the API.
     */
    private void submitLeaderboard(List<LeaderboardEntry> leaderboard, int totalScore) {
        System.out.println("[INFO] Submitting leaderboard...");

        SubmitRequest request = new SubmitRequest(REG_NO, leaderboard);
        SubmitResponse response = apiClient.submitLeaderboard(request);

        System.out.println();
        System.out.println("==================== SUBMISSION RESULT ====================");

        if (response != null) {
            System.out.println("isCorrect    : " + response.isCorrect());
            System.out.println("isIdempotent : " + response.isIdempotent());
            System.out.println("Submitted    : " + response.getSubmittedTotal());
            System.out.println("Expected     : " + response.getExpectedTotal());
            System.out.println("Message      : " + response.getMessage());
        } else {
            System.out.println("[ERROR] No response received from server.");
        }

        System.out.println("===========================================================");
    }
}
