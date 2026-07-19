# Metanoia — Design Doc v0.1 (MVP)

## Visión General

App web personal para devocionales diarios + estudio bíblico con 2 versiones (FBV y RV1909). Clon personalizado de Logos Bible Software enfocado en: búsqueda bíblica, diario devocional con citas de versículos, y multi-panel sincronizado.

## Stack

| Capa | Tecnología |
|---|---|
| Frontend | React 19 + Vite + Tailwind CSS 4 |
| Backend | Spring Boot 3.4 + Java 21 |
| DB | PostgreSQL 16 + Flyway migrations |
| Search | PostgreSQL Full-Text Search (tsvector) |
| Container | Docker Compose |
| Diseño | Tailwind + Open Design (generación de UI) |

## Esquema DB

### bible_versions
| Columna | Tipo | Descripción |
|---|---|---|
| id | SERIAL PK | |
| slug | VARCHAR(20) UNIQUE | 'fbv', 'rv1909' |
| name | VARCHAR(100) | 'Versión Biblia Libre', 'Reina-Valera 1909' |
| language | VARCHAR(10) | 'es' |
| license | VARCHAR(50) | 'CC BY-SA 4.0', 'public-domain' |
| metadata | JSONB | datos adicionales |

### books
| Columna | Tipo | Descripción |
|---|---|---|
| id | SERIAL PK | |
| osis_id | VARCHAR(10) UNIQUE | 'Gen', 'Exod', 'Matt', 'John'... |
| name | VARCHAR(100) | 'Génesis', 'Juan'... |
| testament | SMALLINT | 1=AT, 2=NT |
| position | SMALLINT | orden dentro del testamento |

### verses
| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGSERIAL PK | |
| bible_version_id | INT FK→bible_versions | |
| book_id | INT FK→books | |
| chapter | SMALLINT | |
| verse | SMALLINT | |
| text | TEXT | |
| search_vector | TSVECTOR | índice de búsqueda full-text español |
| UNIQUE | (bible_version_id, book_id, chapter, verse) | |

### devotionals
| Columna | Tipo | Descripción |
|---|---|---|
| id | UUID PK | gen_random_uuid() |
| date | DATE UNIQUE | |
| title | VARCHAR(255) | |
| content | TEXT | HTML enriquecido |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

### devotional_verses
| Columna | Tipo | Descripción |
|---|---|---|
| devotional_id | UUID FK→devotionals ON DELETE CASCADE | |
| verse_id | BIGINT FK→verses | |
| bible_version_id | INT FK→bible_versions | |
| reference_text | VARCHAR(50) | ej. "Juan 3:16" |
| PRIMARY KEY | (devotional_id, verse_id) | |

## API REST

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

## Layout UI (3 paneles)

```
┌─ Dashboard Bar ──────────────────────────────────────────┐
│ [Logo]  [⌘K Buscar]  [FBV] [RV1909]  [📅 Fecha]        │
├───────────────────┬──────────────────┬─────────────────────┤
│ PANEL A (Biblia)  │ PANEL B (Notas)  │ PANEL C (Refs)     │
│ FBV │ RV1909      │ 📓 Devocional    │ · Versículos       │
│ Parallel view     │ Título: [____]   │   relacionados     │
│ Gn 1:1            │ Editor rich text │ · Comentario       │
│ "En el..."        │ (Tiptap)         │   automático       │
│                   │ Versículos:      │                    │
│                   │ · Juan 3:16 ✕   │                    │
├───────────────────┴──────────────────┴─────────────────────┤
│ ⌘B Buscar  ⌘N Nota  ⌘D Dashboard  ⌘S Guardar  ⌥1-3 Panel  │
└────────────────────────────────────────────────────────────┘
```

## Hotkeys

| Atajo | Acción |
|---|---|
| ⌘K | Command Palette (búsqueda global) |
| ⌘B | Enfocar buscador bíblico |
| ⌘N | Nuevo devocional |
| ⌘S | Guardar devocional actual |
| ⌘D | Dashboard |
| ⌥1 | Panel A (Biblia) |
| ⌥2 | Panel B (Devocional) |
| ⌥3 | Panel C (Referencias) |
| ⌘⇧F | Búsqueda avanzada en toda la Biblia |
| Esc | Cerrar buscador/panel flotante |

## Roadmap post-MVP

1. Multi-usuario (auth simple)
2. Planes de lectura (Biblia en 1 año)
3. Sincronización en tiempo real
4. APK mobile (vista responsiva)
5. Plugin de diccionario/comentario bíblico
