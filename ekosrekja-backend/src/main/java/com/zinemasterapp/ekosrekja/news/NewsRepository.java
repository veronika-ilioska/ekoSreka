package com.zinemasterapp.ekosrekja.news;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByPublishedTrue(Pageable pageable);

    Page<News> findByPublishedTrueAndCategoryIgnoreCase(String category, Pageable pageable);
}
