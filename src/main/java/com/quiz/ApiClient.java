package com.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client for interacting with the Quiz Validator API.
 *
 * Base URL: https://devapigw.vidalhealthtpa.com/srm-quiz-task
 *   GET  /quiz/messages?regNo=&poll=
 *   POST /quiz/submit
 */
public class ApiClient {

    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches quiz events for a given poll index.
     *
     * GET /quiz/messages?regNo={regNo}&poll={pollIndex}
     *
     * @param regNo     Registration number
     * @param pollIndex Poll index (0–9)
     * @return PollResponse or null on failure
     */
    public PollResponse fetchPoll(String regNo, int pollIndex) {
        String url = BASE_URL + "/quiz/messages?regNo=" + regNo + "&poll=" + pollIndex;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build();

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.printf("[WARN] Poll %d attempt %d: HTTP %d%n", pollIndex, attempt, response.code());
                    if (attempt < maxRetries) {
                        Thread.sleep(2000);
                        continue;
                    }
                    return null;
                }

                String body = response.body() != null ? response.body().string() : null;
                if (body == null || body.isBlank()) {
                    System.err.printf("[WARN] Poll %d: Empty response body%n", pollIndex);
                    return null;
                }

                return objectMapper.readValue(body, PollResponse.class);

            } catch (IOException e) {
                System.err.printf("[ERROR] Poll %d attempt %d: %s%n", pollIndex, attempt, e.getMessage());
                if (attempt < maxRetries) {
                    try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return null;
    }

    /**
     * Submits the leaderboard to the validator API.
     *
     * POST /quiz/submit
     *
     * @param submitRequest The leaderboard payload
     * @return SubmitResponse or null on failure
     */
    public SubmitResponse submitLeaderboard(SubmitRequest submitRequest) {
        String url = BASE_URL + "/quiz/submit";

        try {
            String jsonBody = objectMapper.writeValueAsString(submitRequest);
            System.out.println("[INFO] Submit payload: " + jsonBody);

            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : null;

                System.out.printf("[INFO] Submit HTTP status: %d%n", response.code());
                System.out.println("[INFO] Submit raw response: " + responseBody);

                if (responseBody == null || responseBody.isBlank()) {
                    System.err.println("[ERROR] Empty response from submit endpoint.");
                    return null;
                }

                return objectMapper.readValue(responseBody, SubmitResponse.class);
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to submit leaderboard: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
