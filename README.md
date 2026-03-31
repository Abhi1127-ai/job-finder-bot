# Job-Finder AI Bot

An autonomous recruitment agent powered by **Spring Boot, Google Gemini, and Playwright**.
Job-Finder AI is a high-performance **automation system** designed to eliminate the most exhausting part of job hunting — discovering, filtering, and analyzing job postings.
It works 24/7, scraping live data, evaluating it against your resume using AI, and notifying you instantly when a strong match is found.
---
## 🛠 Tech Stack

* **Backend:** Java 21 + Spring Boot 3.3.5
* **AI Engine:** Spring AI (Google Gemini 1.5 Flash)
* **Web Automation:** Microsoft Playwright
* **Database:** MongoDB Atlas + Vector Store
* **Notifications:** Telegram Bot API
* **Scheduling:** Spring Scheduler (Cron Jobs)
---
##  Key Features

###  1. Smart Scraper (Playwright)
* Uses **persistent browser sessions**
* Maintains login state (e.g., LinkedIn)
* Extracts dynamic job descriptions hidden behind JS
* Mimics real user behavior to reduce bot detection
---
###  2. AI-Powered Matching (RAG)

* Each job is analyzed using **Google Gemini**
* Compares job requirements with your resume
* Generates:

    *  Match Score (1–10)
    *  Short technical justification
---
###  3. Automated Notifications

* Sends instant alerts via Telegram
* Triggered when match score ≥ 8
* Includes:

    * Job title
    * Match score
    * Direct apply link
---
###  4. Semantic Search (Vector DB)

* Stores high-quality matches as embeddings
* Enables smart queries like:

    * *“Find jobs involving Microservices and AWS”*
* Works even without exact keyword match
---
## System Architecture

1. **Trigger**
   `JobBotScheduler` runs based on a cron schedule

2. **Extract**
   `LinkedInScraper` fetches latest jobs

3. **Filter**
   `ScraperService` removes duplicates using MongoDB

4. **Score**
   `JobMatchService` evaluates jobs using AI

5. **Notify**
   `TelegramNotificationService` sends alerts
---
##  Getting Started

###  Prerequisites

* JDK 21
* Maven 3.9+
* MongoDB Atlas account
* Google Gemini API Key
* Telegram Bot Token & Chat ID
---

### ⚙ Configuration

Update `application.properties`:

```properties
# MongoDB
spring.data.mongodb.uri=${MONGO_URI}

# Gemini AI
spring.ai.vertex.ai.project-id=${PROJECT_ID}
spring.ai.vertex.ai.location=us-central1

# Telegram
telegram.bot.token=${BOT_TOKEN}
telegram.chat.id=${CHAT_ID}
```

---

### ▶ Run the Project

```bash
mvn clean install
mvn spring-boot:run
```

---

##  API Endpoints

| Method | Endpoint         | Description                      |
| ------ | ---------------- | -------------------------------- |
| POST   | /api/jobs/hunt   | Trigger job scraping manually    |
| GET    | /api/jobs/search | Search jobs using semantic query |

---

##  Project Goal

The goal of this project is to shift from:

> ❌ Manual Job Searching
> ✅ Intelligent Job Discovery

By automating the discovery phase, users can focus entirely on **interview preparation and career growth**.
---
##  Support
If you like this project, consider giving it a ⭐ on GitHub!
