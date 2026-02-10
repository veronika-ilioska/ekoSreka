package com.zinemasterapp.ekosrekja.quiz.repo;

import com.zinemasterapp.ekosrekja.quiz.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {}

