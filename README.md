# 🏆 Quiz Leaderboard System

> **Internship Assignment — Java Qualifier**  
> Registration Number: `AP23110011425`

A production-grade Java application that polls a distributed quiz validator API, handles idempotent deduplication of events, aggregates participant scores, and submits a verified leaderboard — all with structured logging and retry-safe HTTP.

---

## 📋 Table of Contents

- [Problem Statement](#-problem-statement)
- [Solution Architecture](#-solution-architecture)
- [Project Structure](#-project-structure)
- [Tech Stack](#-tech-stack)
- [How It Works](#-how-it-works)
- [Deduplication Strategy](#-deduplication-strategy)
- [Getting Started](#-getting-started)
- [Sample Output](#-sample-output)
- [Design Decisions](#-design-decisions)

---

## 🎯 Problem Statement

A quiz validator API delivers participant score events across **10 sequential polls**. Due to the nature of distributed systems, the **same event data may be delivered in multiple polls**. The challenge is to:

1. Poll the API exactly 10 times (poll indices `0–9`), with a **mandatory 5-second delay** between each call
2. Correctly **deduplicate** repeated events using a composite key of `(roundId, participant)`
3. **Aggregate** unique scores per participant
4. Build a **sorted leaderboard** and submit it exactly once

Processing duplicates naively inflates scores and produces wrong results. This solution handles it correctly.

---

## 🏗 Solution Architecture

```
QuizLeaderboardApp (main)
        │
        └──▶ QuizService.run()
                │
                ├── pollAllRounds()        ← 10 API calls, 5s delay each
                │       └── ApiClient.fetchPoll()
                │
                ├── deduplicateEvents()    ← composite key: roundId|participant
                │
                ├── aggregateScores()      ← Map<participant, totalScore>
                │
                ├── buildLeaderboard()     ← sorted descending by score
                │
                ├── printLeaderboard()     ← console verification table
                │
                └── submitLeaderboard()    ← single POST to /quiz/submit
                        └── ApiClient.submitLeaderboard()
```

---

## 📁 Project Structure

```
quiz-leaderboard/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/quiz/
                ├── QuizLeaderboardApp.java   # Entry point (main)
                ├── QuizService.java          # Core orchestration logic
                ├── ApiClient.java            # HTTP client (OkHttp + retry)
                ├── PollResponse.java         # Deserialised GET response
                ├── QuizEvent.java            # Single score event (roundId, participant, score)
                ├── LeaderboardEntry.java     # Participant + totalScore
                ├── SubmitRequest.java        # POST /quiz/submit payload
                └── SubmitResponse.java       # Validator response model
```

---

## 🛠 Tech Stack

| Dependency | Version | Purpose |
|---|---|---|
| Java | 11 | Language & runtime |
| Maven | 3.x | Build & dependency management |
| OkHttp | 4.12.0 | HTTP client with connection pooling |
| Jackson Databind | 2.17.0 | JSON serialisation / deserialisation |
| SLF4J Simple | 2.0.13 | Lightweight logging |
| Maven Shade Plugin | 3.5.2 | Fat JAR packaging |

---

## ⚙️ How It Works

### Step 1 — Poll (10 times, 5s apart)
```
GET /quiz/messages?regNo=AP23110011425&poll=0
GET /quiz/messages?regNo=AP23110011425&poll=1
...
GET /quiz/messages?regNo=AP23110011425&poll=9
```
Each response returns a list of events: `{ roundId, participant, score }`.  
All events from all 10 polls are accumulated into a single raw list.

### Step 2 — Deduplicate
A composite key `roundId + "|" + participant` is inserted into a `LinkedHashSet`.  
Any event whose key has already been seen is **silently dropped** — preserving insertion order for the rest.

### Step 3 — Aggregate
Unique events are merged into a `Map<String, Integer>` using `Map.merge(..., Integer::sum)`, summing scores per participant.

### Step 4 — Sort & Submit
The leaderboard is sorted **descending by totalScore**, with **alphabetical tie-breaking**.  
A single `POST /quiz/submit` delivers the final result.

---

## 🔑 Deduplication Strategy

This is the core challenge of the assignment.

```
Raw events received:
  Poll 0 → R1 | Alice +10
  Poll 0 → R1 | Bob   +20
  Poll 3 → R1 | Alice +10  ← DUPLICATE (same roundId + participant)
  Poll 7 → R1 | Bob   +20  ← DUPLICATE

After deduplication:
  R1 | Alice → 10  ✓
  R1 | Bob   → 20  ✓

Final leaderboard:
  1. Bob    20
  2. Alice  10
  Grand Total: 30
```

**Key:** `String key = event.getRoundId() + "|" + event.getParticipant();`

Using a `LinkedHashSet<String>` ensures O(1) lookup and stable insertion-order preservation.

---

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Internet access (to reach the validator API)

### Clone & Build

```bash
git clone https://github.com/your-username/quiz-leaderboard.git
cd quiz-leaderboard
mvn clean package -q
```

This produces two JARs in `target/`:
- `quiz-leaderboard.jar` — thin JAR (requires `libs/` classpath)
- `quiz-leaderboard-fat.jar` — standalone fat JAR ✅ (use this)

### Run

```bash
java -jar target/quiz-leaderboard-fat.jar
```

> ⚠️ The program takes approximately **50 seconds** to complete — this is expected due to the mandatory 5-second delay between each of the 10 polls.

---

## 🖥 Sample Output

```
========================================
  Quiz Leaderboard System
  Registration No: AP23110011425
========================================

[INFO] Starting to poll API (10 polls with 5s delay between each)...

[POLL 1/10] Fetching data for poll=0...
           Received 3 event(s) from setId=SET_1
           Waiting 5 seconds before next poll...
[POLL 2/10] Fetching data for poll=1...
           Received 2 event(s) from setId=SET_1
           Waiting 5 seconds before next poll...
...
[POLL 10/10] Fetching data for poll=9...
           Received 3 event(s) from setId=SET_1

[INFO] Total raw events collected (before dedup): 28

[DEDUP] Skipping duplicate: roundId=R1, participant=Alice, score=10
[DEDUP] Skipping duplicate: roundId=R2, participant=Bob,   score=30
...

[INFO] Duplicates removed: 10
[INFO] Unique events after deduplication: 18
[INFO] Score aggregation complete. Participants: 5

==================== LEADERBOARD ====================
Rank  Participant          Total Score
------------------------------------------------------
1     Charlie              150
2     Bob                  120
3     Diana                95
4     Alice                70
5     Eve                  40
------------------------------------------------------
      GRAND TOTAL          475
======================================================

[INFO] Submitting leaderboard...
[INFO] Submit HTTP status: 200

==================== SUBMISSION RESULT ====================
isCorrect    : true
isIdempotent : true
Submitted    : 475
Expected     : 475
Message      : Correct!
===========================================================
```

---

## 🧠 Design Decisions

| Decision | Rationale |
|---|---|
| **OkHttp over HttpURLConnection** | Built-in connection pooling, cleaner API, and timeout configuration |
| **LinkedHashSet for dedup** | O(1) contains/add with insertion-order preservation |
| **Map.merge with Integer::sum** | Idiomatic, null-safe score accumulation |
| **Retry logic (3 attempts)** | Guards against transient network failures per poll |
| **Fat JAR via Shade plugin** | Zero-dependency execution — `java -jar` just works |
| **Tie-breaking by name** | Deterministic leaderboard ordering regardless of score ties |
| **Single submit call** | Satisfies the "submit only once" requirement by design |

---

## 📄 API Reference

**Base URL:** `https://devapigw.vidalhealthtpa.com/srm-quiz-task`

| Endpoint | Method | Description |
|---|---|---|
| `/quiz/messages` | GET | Fetch events for a given poll index |
| `/quiz/submit` | POST | Submit the final leaderboard |

**GET params:** `regNo` (string), `poll` (0–9)

**POST body:**
```json
{
  "regNo": "AP23110011425",
  "leaderboard": [
    { "participant": "Charlie", "totalScore": 150 },
    { "participant": "Bob",     "totalScore": 120 }
  ]
}
```

---

*Built for Bajaj Finserv Health | Java Qualifier | SRM | April 2026*
