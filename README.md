# Job-Finder AI Bot

An autonomous recruitment agent powered by **Spring Boot, Groq AI, Google Gemini, and Playwright**.
Job-Finder AI is a high-performance **automation system** designed to eliminate the most exhausting part of job hunting — discovering, filtering, and analyzing job postings.
It works 24/7, scraping live data from multiple platforms, evaluating it against your resume using AI, and notifying you instantly when a strong match is found.

---

## 🛠 Tech Stack

* **Backend:** Java 21 + Spring Boot 3.3.5
* **AI Engine:** Groq AI (llama-3.3-70b-versatile) + Google Gemini
* **Web Automation:** Microsoft Playwright
* **Database:** MongoDB Atlas + Vector Store
* **Notifications:** Telegram Bot API
* **Scheduling:** Spring Scheduler (Cron Jobs)

---

## Key Features

### 1. Multi-Platform Scraper

* Scrapes job listings across multiple platforms — **LinkedIn, Internshala, Unstop**, with more platforms being added over time for wider coverage
* Uses **persistent browser sessions** via Playwright
* Maintains login state where required (e.g., LinkedIn)
* Extracts dynamic job descriptions hidden behind JS
* Mimics real user behavior to reduce bot detection
* Uses direct API calls where available (e.g., Unstop) for faster, more reliable extraction

---

### 2. AI-Powered Matching (RAG)

* Each job is analyzed using **Groq AI / Google Gemini**
* Compares job requirements with your resume
* Generates:

    * Match Score (1–10)
    * Short technical justification

---

### 3. Automated Notifications

* Sends instant alerts via Telegram
* Triggered when match score ≥ 8
* Includes:

    * Job title
    * Match score
    * Direct apply link

---

### 4. Semantic Search (Vector DB)

* Stores high-quality matches as embeddings
* Enables smart queries like:

    * *"Find jobs involving Microservices and AWS"*
* Works even without exact keyword match

---

## System Architecture

1. **Trigger**
   `JobBotScheduler` runs based on a cron schedule

2. **Extract**
   Platform-specific scrapers/clients (LinkedIn, Internshala, Unstop, and more) fetch the latest jobs

3. **Filter**
   `ScraperService` removes duplicates using MongoDB

4. **Score**
   `JobMatchService` evaluates jobs using AI

5. **Notify**
   `TelegramNotificationService` sends alerts

---

## Getting Started

### Prerequisites

* JDK 21
* Maven 3.9+
* MongoDB Atlas account
* Groq API Key
* Google Gemini API Key
* Telegram Bot Token & Chat ID

---

### ⚙ Configuration

Update `application.properties`:

```properties
# MongoDB
spring.data.mongodb.uri=${MONGO_URI}

# Groq AI
groq.api.key=${GROQ_API_KEY}
groq.model=llama-3.3-70b-versatile

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

## API Endpoints

| Method | Endpoint          | Description                       |
|--------|-------------------|------------------------------------|
| POST   | /api/jobs/hunt     | Trigger job scraping manually     |
| GET    | /api/jobs/search   | Search jobs using semantic query  |

---

## Project Goal

The goal of this project is to shift from:

> ❌ Manual Job Searching
> ✅ Intelligent Job Discovery

By automating the discovery phase, users can focus entirely on **interview preparation and career growth**.

---

## Roadmap

* Add more scraping platforms for broader job coverage
* Multi-user support — personalized job alerts for any user, not just one

---

## Support

If you like this project, consider giving it a ⭐ on GitHub!