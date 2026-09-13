ALTER TABLE users
    ALTER COLUMN is_admin SET DEFAULT FALSE;

UPDATE users
SET is_admin = FALSE
WHERE is_admin IS NULL;

ALTER TABLE users
    ALTER COLUMN is_admin SET NOT NULL;

ALTER TABLE quiz_submission
    ADD COLUMN IF NOT EXISTS user_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_quiz_submission_user'
            AND conrelid = 'quiz_submission'::regclass
    ) THEN
        ALTER TABLE quiz_submission
            ADD CONSTRAINT fk_quiz_submission_user
                FOREIGN KEY (user_id)
                    REFERENCES users (id)
                    ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_quiz_submission_user_created
    ON quiz_submission (user_id, created_at DESC);
