DO $$
BEGIN
    IF to_regclass('public.news') IS NOT NULL THEN
DELETE FROM news;

IF pg_get_serial_sequence('news', 'id') IS NOT NULL THEN
            PERFORM setval(pg_get_serial_sequence('news', 'id'), 1, false);
END IF;
END IF;
END $$;