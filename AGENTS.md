# Metanoia — Bible Study & Devotional App

## Stack
- **Frontend:** React 19 + Vite + Tailwind CSS 4
- **Backend:** Spring Boot 3.4 + Java 21
- **DB:** PostgreSQL 16 + Flyway
- **Search:** PostgreSQL Full-Text Search (tsvector)
- **Container:** Docker Compose
- **Design:** Tailwind + Open Design (opencode-design MCP)

## Project Structure
```
metanoia/
├── backend/              # Spring Boot
│   └── src/main/java/com/metanoia/
│       ├── controller/   # REST controllers
│       ├── service/      # Business logic
│       ├── repository/   # JPA repositories
│       ├── model/        # Entities
│       └── config/       # CORS, security, etc
├── frontend/             # React + Vite
│   └── src/
│       ├── components/   # Reusable components
│       ├── pages/        # Dashboard, Devotional, etc
│       ├── hooks/        # Custom hooks
│       ├── store/        # State (Zustand or Context)
│       └── lib/          # Utilities
├── db/
│   └── init/             # Init SQL scripts
├── docker-compose.yml
├── AGENTS.md
└── docs/superpowers/specs/2026-07-18-metanoia-design.md
```

## Database Schema
Tables: bible_versions, books, verses (with tsvector), devotionals, devotional_verses.
See spec doc for full schema.

## API Endpoints
```
GET    /api/bibles/versions
GET    /api/bibles/passage?book=&ch=&v=&version=
GET    /api/bibles/search?q=&version=
GET    /api/devotionals/today
GET    /api/devotionals?date=YYYY-MM-DD
GET    /api/devotionals
POST   /api/devotionals
PUT    /api/devotionals/{id}
POST   /api/devotionals/{id}/verses
DELETE /api/devotionals/{id}/verses/{vid}
```

## Key Design Decisions
- PostgreSQL tsvector for Bible search (Spanish stemming)
- UUID primary keys for user-facing entities
- Flyway for DB migrations
- TipTap editor for rich text in devotionals
- Zustand for global state (active verse, active panel)
- Link Sets concept: Panel A and Panel C sync via shared state

## Keyboard Shortcuts
⌘K Command Palette | ⌘B Search Bible | ⌘N New devotional | ⌘S Save | ⌘D Dashboard | ⌥1-3 Panels
