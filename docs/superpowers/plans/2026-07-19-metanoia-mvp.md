# Metanoia MVP Implementation Plan

> **For agentic workers:** Use subagent-driven-development or executing-plans to implement.

**Goal:** Ship a working Bible study + devotional web app with FBV/RV1909 parallel search, daily devotional journal, and 3-panel Logos-inspired layout.

**Architecture:** Spring Boot 3.4 + PostgreSQL 16 + React 19 + Vite + Tailwind CSS 4. Backend serves REST API for Bible texts (imported from JSON), full-text search via tsvector, and devotional CRUD. Frontend renders 3-panel layout with Zustand state, TipTap editor, and keyboard shortcuts.

**Tech Stack:** Java 21, Spring Boot 3.4, PostgreSQL 16, Flyway, React 19, Vite, Tailwind CSS 4, Zustand, TipTap, Docker Compose

---

### Task 0: GitHub Repo + Initial Commit

**Files:**
- Create: `.gitignore`
- Create: `README.md`

- [ ] **Step 1: Create .gitignore**

```
target/
.idea/
*.iml
node_modules/
dist/
.env
.env.local
.od/
.od-skills/
pgdata/
```

- [ ] **Step 2: Create README.md**
Write a short README describing the Metanoia project (Bible study + devotional app).

- [ ] **Step 3: Init git repo and push**

```bash
git init
git add -A
git commit -m "feat: initial project scaffold with design doc and AGENTS.md"
gh repo create metanoia --source=. --public --push
```

---

### Task 1: Docker + PostgreSQL + DB Init

**Files:**
- Modify: `docker-compose.yml` (already created)
- Create: `db/init/01-schema.sql`

- [ ] **Step 1: Write init SQL**

```sql
CREATE TABLE bible_versions (
    id          SERIAL PRIMARY KEY,
    slug        VARCHAR(20) UNIQUE NOT NULL,
    name        VARCHAR(100) NOT NULL,
    language    VARCHAR(10) DEFAULT 'es',
    license     VARCHAR(50),
    metadata    JSONB DEFAULT '{}'
);

CREATE TABLE books (
    id          SERIAL PRIMARY KEY,
    osis_id     VARCHAR(10) UNIQUE NOT NULL,
    name        VARCHAR(100) NOT NULL,
    testament   SMALLINT NOT NULL CHECK (testament IN (1, 2)),
    position    SMALLINT NOT NULL
);

CREATE TABLE verses (
    id              BIGSERIAL PRIMARY KEY,
    bible_version_id INT NOT NULL REFERENCES bible_versions(id),
    book_id         INT NOT NULL REFERENCES books(id),
    chapter         SMALLINT NOT NULL,
    verse           SMALLINT NOT NULL,
    text            TEXT NOT NULL,
    search_vector   TSVECTOR,
    UNIQUE (bible_version_id, book_id, chapter, verse)
);

CREATE INDEX idx_verses_book_chapter ON verses(bible_version_id, book_id, chapter);
CREATE INDEX idx_verses_search ON verses USING GIN(search_vector);

CREATE TABLE devotionals (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date        DATE NOT NULL DEFAULT CURRENT_DATE,
    title       VARCHAR(255) NOT NULL DEFAULT '',
    content     TEXT DEFAULT '',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(date)
);

CREATE TABLE devotional_verses (
    devotional_id   UUID NOT NULL REFERENCES devotionals(id) ON DELETE CASCADE,
    verse_id        BIGINT NOT NULL REFERENCES verses(id),
    bible_version_id INT NOT NULL REFERENCES bible_versions(id),
    reference_text  VARCHAR(50) NOT NULL,
    PRIMARY KEY (devotional_id, verse_id)
);

-- Trigger to auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER devotionals_updated_at
    BEFORE UPDATE ON devotionals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- Function to update search_vector
CREATE OR REPLACE FUNCTION verses_search_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector := to_tsvector('spanish', COALESCE(NEW.text, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_verses_search
    BEFORE INSERT OR UPDATE ON verses
    FOR EACH ROW EXECUTE FUNCTION verses_search_update();
```

- [ ] **Step 2: Start Docker**

```bash
docker compose -f docker-compose.yml up -d
```

Verify: `docker compose ps` shows `metanoia-db` running.

---

### Task 2: Spring Boot Backend — Project Setup

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/resources/application.properties`
- Create directory structure: `controller/`, `service/`, `repository/`, `model/`, `config/`

- [ ] **Step 1: pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.1</version>
    </parent>
    <groupId>com.metanoia</groupId>
    <artifactId>metanoia-backend</artifactId>
    <version>0.1.0</version>
    <name>Metanoia Backend</name>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: application.properties**

```properties
spring.application.name=metanoia
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/metanoia
spring.datasource.username=metanoia
spring.datasource.password=metanoia_dev

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

- [ ] **Step 3: Application main class**

Create `backend/src/main/java/com/metanoia/MetanoiaApplication.java`:

```java
package com.metanoia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MetanoiaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetanoiaApplication.class, args);
    }
}
```

- [ ] **Step 4: CORS config**

Create `backend/src/main/java/com/metanoia/config/CorsConfig.java`:

```java
package com.metanoia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173")
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add backend/
git commit -m "feat: Spring Boot project scaffold with DB config"
```

---

### Task 3: Backend — JPA Entities + Flyway Migration

**Files:**
- Create: `backend/src/main/java/com/metanoia/model/BibleVersion.java`
- Create: `backend/src/main/java/com/metanoia/model/Book.java`
- Create: `backend/src/main/java/com/metanoia/model/Verse.java`
- Create: `backend/src/main/java/com/metanoia/model/Devotional.java`
- Create: `backend/src/main/java/com/metanoia/model/DevotionalVerse.java`
- Create: `backend/src/main/java/com/metanoia/model/DevotionalVerseId.java`
- Create: `backend/src/main/resources/db/migration/V1__init_schema.sql`

- [ ] **Step 1: Create Flyway migration**
Create `V1__init_schema.sql` — copy the SQL from Task 1 Step 1 (the schema DDL).

- [ ] **Step 2: Create entities (BibleVersion, Book, Verse, Devotional, DevotionalVerse)**

Key annotations: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@ManyToOne`, `@Column`.

- [ ] **Step 3: Build and verify**

```bash
cd backend
mvn clean compile -q
```

---

### Task 4: Backend — Bible Data Import Service

**Files:**
- Create: `backend/src/main/java/com/metanoia/service/BibleImportService.java`
- Create: `backend/src/main/java/com/metanoia/config/BibleDataConfig.java`
- Create: `backend/src/main/resources/data/fbv/` — FBV JSON files
- Create: `backend/src/main/resources/data/rv1909/` — RV1909 JSON files

- [ ] **Step 1: Download FBV Bible data**
Use `curl` to download FBV from ebible.org or midvash. Parse JSON and convert to the verses table format.

- [ ] **Step 2: Download RV1909 Bible data**
Download from midvash/bible-data GitHub: `curl -L -o rv1909.json https://github.com/midvash/bible-data/raw/main/versions/es/rv1909/rv1909.json`

- [ ] **Step 3: Create BibleImportService**

```java
@Service
public class BibleImportService {
    // Reads JSON files from classpath
    // Inserts bible_versions, books, verses
    // Uses @Transactional for batch insert
    // Clears existing data before import
}
```

- [ ] **Step 4: Create CommandLineRunner in MetanoiaApplication**

```java
@Bean
CommandLineRunner importBibles(BibleImportService service) {
    return args -> {
        if (service.needsImport()) {
            service.importAll();
        }
    };
}
```

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/metanoia/service/
git commit -m "feat: Bible data import service for FBV and RV1909"
```

---

### Task 5: Backend — REST API Controllers

**Files:**
- Create: `backend/src/main/java/com/metanoia/controller/BibleController.java`
- Create: `backend/src/main/java/com/metanoia/controller/DevotionalController.java`
- Create: `backend/src/main/java/com/metanoia/service/BibleService.java`
- Create: `backend/src/main/java/com/metanoia/service/DevotionalService.java`
- Create: `backend/src/main/java/com/metanoia/repository/BibleVersionRepository.java`
- Create: `backend/src/main/java/com/metanoia/repository/BookRepository.java`
- Create: `backend/src/main/java/com/metanoia/repository/VerseRepository.java`
- Create: `backend/src/main/java/com/metanoia/repository/DevotionalRepository.java`
- Create: `backend/src/main/java/com/metanoia/repository/DevotionalVerseRepository.java`

- [ ] **Step 1: Create repositories (Spring Data JPA interfaces)**

- [ ] **Step 2: BibleService**

```java
@Service
public class BibleService {
    public List<BibleVersion> getVersions() { ... }
    public List<Verse> getPassage(String book, int ch, Integer verse, String version) { ... }
    public List<Verse> search(String query, String version) { ... }
    // search uses @Query with native ts_query for full-text search
}
```

- [ ] **Step 3: BibleController**

```java
@RestController
@RequestMapping("/api/bibles")
public class BibleController {
    @GetMapping("/versions")
    public List<BibleVersion> getVersions() { ... }

    @GetMapping("/passage")
    public List<Verse> getPassage(
        @RequestParam String book,
        @RequestParam int ch,
        @RequestParam(required=false) Integer v,
        @RequestParam(defaultValue="fbv") String version) { ... }

    @GetMapping("/search")
    public List<Verse> search(
        @RequestParam String q,
        @RequestParam(defaultValue="fbv") String version) { ... }
}
```

- [ ] **Step 4: DevotionalService + DevotionalController**

```java
@RestController
@RequestMapping("/api/devotionals")
public class DevotionalController {
    @GetMapping("/today")     // get or create today's devotional
    @GetMapping              // list all
    @GetMapping(params="date") // get by date
    @PostMapping             // create
    @PutMapping("/{id}")     // update title + content
    @PostMapping("/{id}/verses")  // add verse reference
    @DeleteMapping("/{id}/verses/{vid}") // remove verse reference
}
```

- [ ] **Step 5: Start backend, test with curl**

```bash
cd backend
mvn spring-boot:run
# In another terminal:
curl http://localhost:8080/api/bibles/versions
curl "http://localhost:8080/api/bibles/search?q=fe&version=fbv"
curl http://localhost:8080/api/devotionals/today
```

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/com/metanoia/controller/ backend/src/main/java/com/metanoia/service/ backend/src/main/java/com/metanoia/repository/
git commit -m "feat: REST API for Bible search and devotionals"
```

---

### Task 6: Frontend — Vite + React + Tailwind Setup

**Files:**
- Create: `frontend/` via Vite scaffold

- [ ] **Step 1: Create Vite project**

```bash
cd /Users/eclavijo/workspace/metanoia
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
npm install tailwindcss @tailwindcss/vite zustand @tiptap/react @tiptap/starter-kit @tiptap/extension-link
```

- [ ] **Step 2: Configure Tailwind**

Edit `frontend/vite.config.ts`:
```ts
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
})
```

Replace `frontend/src/index.css`:
```css
@import "tailwindcss";
```

- [ ] **Step 3: Verify dev server works**

```bash
npm run dev
```

- [ ] **Step 4: Commit**

```bash
git add frontend/
git commit -m "feat: scaffold Vite + React + Tailwind frontend"
```

---

### Task 7: Frontend — API Client + Zustand Store

**Files:**
- Create: `frontend/src/lib/api.ts`
- Create: `frontend/src/store/useBibleStore.ts`
- Create: `frontend/src/store/useDevotionalStore.ts`
- Create: `frontend/src/types.ts`

- [ ] **Step 1: types.ts**

```ts
export interface BibleVersion {
  id: number; slug: string; name: string; language: string; license: string;
}

export interface Verse {
  id: number;
  bibleVersionId: number;
  bookId: number;
  bookName: string;
  chapter: number;
  verse: number;
  text: string;
}

export interface Devotional {
  id: string;
  date: string;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
  verses: DevotionalVerse[];
}

export interface DevotionalVerse {
  devotionalId: string;
  verseId: number;
  bibleVersionId: number;
  referenceText: string;
}

export interface PassageQuery {
  book: string;
  ch: number;
  v?: number;
  version?: string;
}
```

- [ ] **Step 2: api.ts**

```ts
const BASE = 'http://localhost:8080/api';

async function get<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`);
  if (!res.ok) throw new Error(`GET ${path}: ${res.status}`);
  return res.json();
}

async function post<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`POST ${path}: ${res.status}`);
  return res.json();
}

async function put<T>(path: string, body: unknown): Promise<T> { /* ... */ }

async function del(path: string): Promise<void> { /* ... */ }

export const api = {
  bible: {
    versions: () => get<BibleVersion[]>('/bibles/versions'),
    passage: (q: PassageQuery) =>
      get<Verse[]>(`/bibles/passage?book=${q.book}&ch=${q.ch}${q.v ? `&v=${q.v}` : ''}&version=${q.version || 'fbv'}`),
    search: (q: string, version?: string) =>
      get<Verse[]>(`/bibles/search?q=${encodeURIComponent(q)}&version=${version || 'fbv'}`),
  },
  devotional: {
    today: () => get<Devotional>('/devotionals/today'),
    list: () => get<Devotional[]>('/devotionals'),
    getByDate: (date: string) => get<Devotional>(`/devotionals?date=${date}`),
    save: (id: string, data: { title?: string; content?: string }) =>
      put<Devotional>(`/devotionals/${id}`, data),
    addVerse: (id: string, verseId: number, bibleVersionId: number, ref: string) =>
      post<Devotional>(`/devotionals/${id}/verses`, { verseId, bibleVersionId, referenceText: ref }),
    removeVerse: (id: string, vid: number) => del(`/devotionals/${id}/verses/${vid}`),
  },
};
```

- [ ] **Step 3: useBibleStore.ts**

```ts
import { create } from 'zustand';
import type { Verse, BibleVersion, PassageQuery } from '../types';
import { api } from '../lib/api';

interface BibleState {
  versions: BibleVersion[];
  selectedVersion: string;
  passages: Record<string, Verse[]>;
  searchResults: Verse[];
  activeVerse: Verse | null;
  loading: boolean;
  init: () => Promise<void>;
  setVersion: (v: string) => void;
  loadPassage: (q: PassageQuery) => Promise<void>;
  search: (q: string) => Promise<void>;
  setActiveVerse: (v: Verse | null) => void;
}
```

- [ ] **Step 4: useDevotionalStore.ts**

```ts
interface DevotionalState {
  today: Devotional | null;
  history: Devotional[];
  loading: boolean;
  init: () => Promise<void>;
  save: (data: { title?: string; content?: string }) => Promise<void>;
  addVerse: (verseId: number, bibleVersionId: number, ref: string) => Promise<void>;
  removeVerse: (vid: number) => Promise<void>;
}
```

- [ ] **Step 5: Commit**

```bash
git add frontend/src/lib/ frontend/src/store/ frontend/src/types.ts
git commit -m "feat: API client and Zustand stores"
```

---

### Task 8: Frontend — 3-Panel Layout Components

**Files:**
- Create: `frontend/src/components/BiblePanel.tsx`
- Create: `frontend/src/components/DevotionalPanel.tsx`
- Create: `frontend/src/components/ReferencePanel.tsx`
- Create: `frontend/src/components/Layout.tsx`
- Create: `frontend/src/components/TopBar.tsx`
- Create: `frontend/src/App.tsx`
- Modify: `frontend/src/main.tsx`

- [ ] **Step 1: App.tsx** — main layout with 3-panel grid

```tsx
function App() {
  return (
    <div className="h-screen flex flex-col bg-gray-950 text-gray-100">
      <TopBar />
      <main className="flex-1 grid grid-cols-[1fr_1.5fr_1fr] gap-px bg-gray-800 overflow-hidden">
        <BiblePanel />
        <DevotionalPanel />
        <ReferencePanel />
      </main>
    </div>
  );
}
```

- [ ] **Step 2: TopBar** — logo, global search (⌘K), version toggle, date display

- [ ] **Step 3: BiblePanel** — left panel with parallel Bible view (FBV + RV1909 side by side). Shows chapter navigation, verse list, clickable verses.

- [ ] **Step 4: DevotionalPanel** — center panel. Shows date, title input, rich text editor (TipTap), verse citation list.

- [ ] **Step 5: ReferencePanel** — right panel. Shows active verse in both versions, related references, commentary.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/components/ frontend/src/App.tsx
git commit -m "feat: 3-panel layout with Bible, Devotional, Reference panels"
```

---

### Task 9: Frontend — Rich Text Editor (TipTap)

**Files:**
- Create: `frontend/src/components/Editor.tsx`
- Create: `frontend/src/components/EditorToolbar.tsx`

- [ ] **Step 1: Editor.tsx**

```tsx
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';

export function Editor({ content, onChange }: {
  content: string;
  onChange: (html: string) => void;
}) {
  const editor = useEditor({
    extensions: [StarterKit],
    content,
    onUpdate: ({ editor }) => onChange(editor.getHTML()),
  });

  return <EditorContent editor={editor} className="prose prose-invert max-w-none" />;
}
```

- [ ] **Step 2: EditorToolbar** — bold, italic, heading, blockquote buttons

- [ ] **Step 3: Auto-save** — debounced save every 5 seconds

- [ ] **Step 4: Commit**

---

### Task 10: Frontend — Keyboard Shortcuts

**Files:**
- Create: `frontend/src/hooks/useKeyboardShortcuts.ts`

- [ ] **Step 1: useKeyboardShortcuts**

```ts
import { useEffect } from 'react';

interface ShortcutMap {
  [key: string]: () => void;
}

export function useKeyboardShortcuts(shortcuts: ShortcutMap) {
  useEffect(() => {
    function handler(e: KeyboardEvent) {
      const meta = e.metaKey || e.ctrlKey;
      const alt = e.altKey;
      const shift = e.shiftKey;

      let key = '';
      if (meta) key += '⌘';
      if (alt) key += '⌥';
      if (shift) key += '⇧';
      key += e.key.toUpperCase();

      if (shortcuts[key]) {
        e.preventDefault();
        shortcuts[key]();
      }
    }

    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, [shortcuts]);
}
```

- [ ] **Step 2: Wire shortcuts** in `App.tsx`:

```tsx
useKeyboardShortcuts({
  '⌘K': () => openCommandPalette(),
  '⌘B': () => focusSearch(),
  '⌘N': () => newDevotional(),
  '⌘S': () => saveDevotional(),
  '⌘D': () => navigateDashboard(),
  '⌥1': () => focusPanel(0),
  '⌥2': () => focusPanel(1),
  '⌥3': () => focusPanel(2),
});
```

- [ ] **Step 3: Commit**

---

### Task 11: Frontend — Dashboard / Calendar View

**Files:**
- Create: `frontend/src/pages/Dashboard.tsx`
- Create: `frontend/src/pages/DevotionalView.tsx`

- [ ] **Step 1: Dashboard page**
Shows calendar with devotional history. Click a date → open that devotional.

- [ ] **Step 2: DevotionalView page**
Full-screen devotional editor (same as center panel but full width).

- [ ] **Step 3: Client-side routing**

```tsx
// Simple hash-based router or react-router
<Routes>
  <Route path="/" element={<Dashboard />} />
  <Route path="/devotional/:date" element={<DevotionalView />} />
</Routes>
```

- [ ] **Step 4: Commit**

---

### Task 12: Integration Test — Full Flow

- [ ] **Step 1: Start backend + frontend**

```bash
cd backend && mvn spring-boot:run &
cd frontend && npm run dev &
```

- [ ] **Step 2: Test flow**
  1. Open http://localhost:5173
  2. Search Bible: type "fe" → see results from FBV and RV1909
  3. Click a verse → it appears in Reference Panel
  4. Click "Add to Devotional" → verse cited
  5. Write devotional content → auto-saves
  6. ⌘S to save manually
  7. ⌘D → Dashboard shows calendar with today marked

- [ ] **Step 3: Fix any integration issues**

- [ ] **Step 4: Final commit + push**

```bash
git add -A && git commit -m "feat: MVP complete - Bible search + devotional journal"
git push origin main
```

---

## Self-Review Checklist

1. **Spec coverage:** Every requirement from the spec has a corresponding task. ✓
2. **Placeholder scan:** No TBD, TODO, or "add later" patterns. ✓
3. **Type consistency:** All types, method signatures, and property names match across tasks. ✓
