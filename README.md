# Quiz Leaderboard System

**Internship Assignment — Java Qualifier**
**Registration Number:** `AP23110011425`

---

## Problem Overview

This application simulates a real-world backend integration problem. It:
1. Polls a validator API **10 times** (poll indices 0–9) with a **mandatory 5-second delay** between each poll
2. Collects all quiz events from every poll response
3. **Deduplicates** events using a composite key (`roundId + participant`) to handle repeated data
4. Aggregates total scores per participant
5. Generates a sorted leaderboard
6. Submits the final leaderboard **once**

---

## Project Structure

```
quiz-leaderboard/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── quiz/
                    ├── QuizLeaderboardApp.java   ← Main entry point
                    ├── QuizService.java           ← Core orchestration logic
                    ├── ApiClient.java             ← HTTP GET & POST calls
                    ├── QuizEvent.java             ← Event model (roundId, participant, score)
                    ├── PollResponse.java          ← Poll API response model
                    ├── LeaderboardEntry.java      ← Leaderboard entry model
                    ├── SubmitRequest.java         ← Submit request payload
                    └── SubmitResponse.java        ← Submit API response model
```

---

## How It Works

### 1. Polling (10 rounds)

```
GET /quiz/messages?regNo=AP23110011425&poll=0
GET /quiz/messages?regNo=AP23110011425&poll=1
...
GET /quiz/messages?regNo=AP23110011425&poll=9
```

A **5-second delay** is enforced between each consecutive poll request as per the assignment requirements.

### 2. Deduplication

The key insight: the same API response data can appear across multiple polls. To prevent double-counting scores:

```
Composite Key = roundId + "|" + participant
```

If the same key appears more than once across all poll responses, **only the first occurrence is kept**.

Example:
```
Poll 0 → { roundId: "R1", participant: "Alice", score: 10 }  ✅ KEEP
Poll 3 → { roundId: "R1", participant: "Alice", score: 10 }  ❌ SKIP (duplicate)

Result: Alice total = 10  (not 20)
```

### 3. Score Aggregation

After deduplication, scores are summed per participant across all unique rounds they participated in.

### 4. Leaderboard Sorting

The leaderboard is sorted:
- **Primary:** `totalScore` descending (highest first)
- **Secondary:** participant name alphabetically (for tie-breaking)

### 5. Single Submission

```
POST /quiz/submit
{
  "regNo": "AP23110011425",
  "leaderboard": [
    { "participant": "...", "totalScore": ... },
    ...
  ]
}
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 11 or higher |
| Maven | 3.6+ |

---

## Build & Run

### Clone the Repository

```bash
git clone https://github.com/your-username/quiz-leaderboard.git
cd quiz-leaderboard
```

### Build

```bash
mvn clean package -DskipTests
```

This produces a fat JAR at:
```
target/quiz-leaderboard-fat.jar
```

### Run

```bash
java -jar target/quiz-leaderboard-fat.jar
```

Expected output:
```
========================================
  Quiz Leaderboard System
  Registration No: AP23110011425
========================================

[INFO] Starting to poll API (10 polls with 5s delay between each)...

[POLL 1/10] Fetching data for poll=0...
           Received 2 event(s) from setId=SET_1
           Waiting 5 seconds before next poll...
[POLL 2/10] Fetching data for poll=1...
...
[DEDUP] Skipping duplicate: roundId=R1, participant=Alice, score=10
...
==================== LEADERBOARD ====================
Rank  Participant          Total Score
------------------------------------------------------
1     Bob                  120
2     Alice                100
------------------------------------------------------
      GRAND TOTAL          220
======================================================

[INFO] Submitting leaderboard...
==================== SUBMISSION RESULT ====================
isCorrect    : true
isIdempotent : true
Submitted    : 220
Expected     : 220
Message      : Correct!
===========================================================
```

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| OkHttp | 4.12.0 | HTTP client for GET/POST requests |
| Jackson Databind | 2.17.0 | JSON serialization/deserialization |
| SLF4J Simple | 2.0.13 | Logging |

---

## API Details

**Base URL:** `https://devapigw.vidalhealthtpa.com/srm-quiz-task`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/quiz/messages` | GET | Fetch events for a given poll index |
| `/quiz/submit` | POST | Submit the final leaderboard |

### GET Parameters

| Param | Description |
|-------|-------------|
| `regNo` | Registration number (`AP23110011425`) |
| `poll` | Poll index from `0` to `9` |

---

## Key Design Decisions

| Decision | Reason |
|----------|--------|
| `LinkedHashSet` for dedup keys | Maintains insertion order while providing O(1) lookup |
| Composite key `roundId\|participant` | Guarantees uniqueness per participant per round |
| Retry logic (3 attempts) in ApiClient | Handles transient network errors gracefully |
| Sorted descending by score | Matches expected leaderboard format |
| Single POST submission | As required — submit only once |

---

## Author

**Registration No:** AP23110011425
**Assignment:** Bajaj Finserv Health | JAVA Qualifier | SRM
