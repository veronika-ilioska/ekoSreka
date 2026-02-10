package com.zinemasterapp.ekosrekja.quiz.repo;

import com.zinemasterapp.ekosrekja.quiz.entity.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
    List<QuizOption> findByQuestionIdOrderByOrdAsc(Long questionId);
}
