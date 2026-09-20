package com.ekosrekja.ekosrekjafullstack.upload;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StoredUploadRepository {
    private final JdbcTemplate jdbc;

    public StoredUploadRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void save(String path, String contentType, byte[] data) {
        jdbc.update("INSERT INTO stored_uploads (path, content_type, data) VALUES (?, ?, ?)",
                path, contentType, data);
    }

    public Optional<Upload> find(String path) {
        return jdbc.query("SELECT content_type, data FROM stored_uploads WHERE path = ?",
                (row, index) -> new Upload(row.getString("content_type"), row.getBytes("data")), path)
                .stream().findFirst();
    }

    public record Upload(String contentType, byte[] data) {}
}
