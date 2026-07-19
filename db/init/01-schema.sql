CREATE TABLE IF NOT EXISTS bible_versions (
    id          SERIAL PRIMARY KEY,
    slug        VARCHAR(20) UNIQUE NOT NULL,
    name        VARCHAR(100) NOT NULL,
    language    VARCHAR(10) DEFAULT 'es',
    license     VARCHAR(50),
    metadata    JSONB DEFAULT '{}'
);

CREATE TABLE IF NOT EXISTS books (
    id          SERIAL PRIMARY KEY,
    osis_id     VARCHAR(10) UNIQUE NOT NULL,
    name        VARCHAR(100) NOT NULL,
    testament   SMALLINT NOT NULL CHECK (testament IN (1, 2)),
    position    SMALLINT NOT NULL
);

CREATE TABLE IF NOT EXISTS verses (
    id              BIGSERIAL PRIMARY KEY,
    bible_version_id INT NOT NULL REFERENCES bible_versions(id),
    book_id         INT NOT NULL REFERENCES books(id),
    chapter         SMALLINT NOT NULL,
    verse           SMALLINT NOT NULL,
    text            TEXT NOT NULL,
    search_vector   TSVECTOR,
    UNIQUE (bible_version_id, book_id, chapter, verse)
);

CREATE INDEX IF NOT EXISTS idx_verses_book_chapter ON verses(bible_version_id, book_id, chapter);
CREATE INDEX IF NOT EXISTS idx_verses_search ON verses USING GIN(search_vector);

CREATE TABLE IF NOT EXISTS devotionals (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date        DATE NOT NULL DEFAULT CURRENT_DATE,
    title       VARCHAR(255) NOT NULL DEFAULT '',
    content     TEXT DEFAULT '',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(date)
);

CREATE TABLE IF NOT EXISTS devotional_verses (
    devotional_id   UUID NOT NULL REFERENCES devotionals(id) ON DELETE CASCADE,
    verse_id        BIGINT NOT NULL REFERENCES verses(id),
    bible_version_id INT NOT NULL REFERENCES bible_versions(id),
    reference_text  VARCHAR(50) NOT NULL,
    PRIMARY KEY (devotional_id, verse_id)
);

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS devotionals_updated_at ON devotionals;
CREATE TRIGGER devotionals_updated_at
    BEFORE UPDATE ON devotionals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE OR REPLACE FUNCTION verses_search_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector := to_tsvector('spanish', COALESCE(NEW.text, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_verses_search ON verses;
CREATE TRIGGER trigger_verses_search
    BEFORE INSERT OR UPDATE ON verses
    FOR EACH ROW EXECUTE FUNCTION verses_search_update();
