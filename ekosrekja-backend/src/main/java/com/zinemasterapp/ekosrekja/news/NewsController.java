package com.ekosrekja.ekosrekjafullstack.news;

import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsRepository repo;

    @GetMapping
    public Page<News> list(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 12) Pageable p) {
        if (category == null || category.isBlank()) {
            return repo.findByPublishedTrue(p);
        }
        return repo.findByPublishedTrueAndCategoryIgnoreCase(category, p);
    }

    @PostMapping
    public News create(@Valid @RequestBody News n) {
        n.setPublished(true);
        n.setPublishedAt(Instant.now());
        return repo.save(n);
    }
}
