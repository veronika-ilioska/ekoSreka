package com.ekosrekja.ekosrekjafullstack.upload;

import java.time.Duration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoredUploadController {
    private final StoredUploadRepository uploads;

    public StoredUploadController(StoredUploadRepository uploads) {
        this.uploads = uploads;
    }

    @GetMapping("/api/media/{folder}/{filename:.+}")
    public ResponseEntity<ByteArrayResource> get(@PathVariable String folder, @PathVariable String filename) {
        return uploads.find(folder + "/" + filename)
                .map(upload -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(upload.contentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                        .body(new ByteArrayResource(upload.data())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
