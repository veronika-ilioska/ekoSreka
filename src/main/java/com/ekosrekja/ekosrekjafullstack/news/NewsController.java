package com.ekosrekja.ekosrekjafullstack.news;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsRepository repo;

    /**
     * Get all published news articles with optional category filter
     */
    @GetMapping
    public Page<News> list(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 12) Pageable p) {
        if (category == null || category.isBlank()) {
            return repo.findAllPublished(p);
        }
        return repo.findByPublishedTrueAndCategoryIgnoreCase(category, p);
    }

    /**
     * Get a single news article by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<News> getById(@PathVariable Long id) {
        return repo.findByIdAndPublishedTrue(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get related articles from the same category
     */
    @GetMapping("/{id}/related")
    public List<News> getRelatedArticles(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        News article = repo.findByIdAndPublishedTrue(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        Pageable pageable = PageRequest.of(page, size);
        return repo.findRelatedArticles(article.getCategory(), pageable);
    }

    /**
     * Get all available categories
     */
    @GetMapping("/categories/all")
    public List<String> getAllCategories() {
        return repo.findAllCategories();
    }

    /**
     * Get news count statistics
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getNewsCount() {
        return ResponseEntity.ok(repo.countByPublishedTrue());
    }

    /**
     * Create a new news article (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<News> create(@Valid @RequestBody News news) {
        news.setPublished(true);
        news.setPublishedAt(Instant.now());
        News saved = repo.save(news);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Update an existing news article (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<News> update(@PathVariable Long id, @Valid @RequestBody News newsDetails) {
        return repo.findById(id)
                .map(news -> {
                    news.setTitle(newsDetails.getTitle());
                    news.setContent(newsDetails.getContent());
                    news.setCategory(newsDetails.getCategory());
                    news.setCoverUrl(newsDetails.getCoverUrl());
                    news.setPublished(newsDetails.isPublished());
                    if (newsDetails.isPublished() && news.getPublishedAt() == null) {
                        news.setPublishedAt(Instant.now());
                    }
                    return ResponseEntity.ok(repo.save(news));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a news article (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return repo.findById(id)
                .map(news -> {
                    repo.delete(news);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Publish/unpublish a news article (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/publish")
    public ResponseEntity<News> togglePublish(@PathVariable Long id) {
        return repo.findById(id)
                .map(news -> {
                    news.setPublished(!news.isPublished());
                    if (news.isPublished() && news.getPublishedAt() == null) {
                        news.setPublishedAt(Instant.now());
                    }
                    return ResponseEntity.ok(repo.save(news));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}



