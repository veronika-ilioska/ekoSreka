ALTER TABLE photos
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

UPDATE photos
SET updated_at = COALESCE(updated_at, created_at, NOW())
WHERE updated_at IS NULL;

ALTER TABLE photos
    ALTER COLUMN updated_at SET DEFAULT NOW();

ALTER TABLE videos
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

UPDATE videos
SET updated_at = COALESCE(updated_at, created_at, NOW())
WHERE updated_at IS NULL;

ALTER TABLE videos
    ALTER COLUMN updated_at SET DEFAULT NOW();

ALTER TABLE games
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

UPDATE games
SET updated_at = COALESCE(updated_at, NOW())
WHERE updated_at IS NULL;

ALTER TABLE games
    ALTER COLUMN updated_at SET DEFAULT NOW();

ALTER TABLE horoscope_entries
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

UPDATE horoscope_entries
SET updated_at = COALESCE(updated_at, created_at, NOW())
WHERE updated_at IS NULL;

ALTER TABLE horoscope_entries
    ALTER COLUMN updated_at SET DEFAULT NOW();

ALTER TABLE quiz
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

UPDATE quiz
SET updated_at = COALESCE(updated_at, NOW())
WHERE updated_at IS NULL;

ALTER TABLE quiz
    ALTER COLUMN updated_at SET DEFAULT NOW();
