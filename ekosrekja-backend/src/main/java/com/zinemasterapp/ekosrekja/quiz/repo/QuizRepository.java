package com.ekosrekja.ekosrekjafullstack.quiz.repo;

import com.ekosrekja.ekosrekjafullstack.quiz.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByActiveTrue(Pageable p);

    Page<Quiz> findByActiveTrueAndLevelIgnoreCase(String level, Pageable p);
}
