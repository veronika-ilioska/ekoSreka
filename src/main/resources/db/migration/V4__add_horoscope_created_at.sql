ALTER TABLE horoscope_entries
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ;

UPDATE horoscope_entries
SET created_at = NOW()
WHERE created_at IS NULL;

ALTER TABLE horoscope_entries
    ALTER COLUMN created_at SET DEFAULT NOW();
