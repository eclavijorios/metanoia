# Metanoia

A personal Bible study and devotional web application, inspired by Logos Bible Software.

## Stack

- **Frontend:** React 19 + Vite + Tailwind CSS 4
- **Backend:** Spring Boot 3.4 + Java 21
- **Database:** PostgreSQL 16 + Flyway
- **Search:** PostgreSQL Full-Text Search (tsvector)
- **Bible Versions:** Versión Biblia Libre (FBV) + Reina-Valera 1909 (RV1909)

## MVP Features

- Multi-panel layout (Bible + Devotional + References)
- Full-text Bible search across two versions (FBV, RV1909)
- Daily devotional journal with rich text editor
- Verse citation system
- Keyboard shortcuts for power users

## Getting Started

```bash
# Start database
docker compose up -d

# Start backend
cd backend && mvn spring-boot:run

# Start frontend
cd frontend && npm run dev
```
