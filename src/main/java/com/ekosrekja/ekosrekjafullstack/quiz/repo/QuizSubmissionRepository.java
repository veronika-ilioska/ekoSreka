package com.ekosrekja.ekosrekjafullstack.quiz.repo;

import com.ekosrekja.ekosrekjafullstack.quiz.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    void deleteByQuizId(Long quizId);
}


