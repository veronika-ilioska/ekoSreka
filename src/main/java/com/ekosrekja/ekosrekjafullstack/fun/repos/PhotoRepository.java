package com.ekosrekja.ekosrekjafullstack.fun.repos;

import com.ekosrekja.ekosrekjafullstack.fun.entity.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Page<Photo> findByTitleContainingIgnoreCase(String q, Pageable p);
}


