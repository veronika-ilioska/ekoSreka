CREATE TABLE stored_uploads (
    path VARCHAR(512) PRIMARY KEY,
    content_type VARCHAR(255) NOT NULL,
    data BYTEA NOT NULL
);
