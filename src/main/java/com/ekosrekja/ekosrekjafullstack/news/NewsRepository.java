package com.ekosrekja.ekosrekjafullstack.news;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByPublishedTrue(Pageable pageable);

    Page<News> findByPublishedTrueAndCategoryIgnoreCase(String category, Pageable pageable);

    Optional<News> findByIdAndPublishedTrue(Long id);

    @Query("SELECT n FROM News n WHERE n.published = true AND n.category = :category ORDER BY n.publishedAt DESC")
    List<News> findRelatedArticles(@Param("category") String category, Pageable pageable);

    @Query("SELECT DISTINCT n.category FROM News n WHERE n.published = true")
    List<String> findAllCategories();

    Long countByPublishedTrue();

    @Query("SELECT n FROM News n WHERE n.published = true ORDER BY n.publishedAt DESC")
    Page<News> findAllPublished(Pageable pageable);
}


