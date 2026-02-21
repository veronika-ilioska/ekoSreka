package com.ekosrekja.ekosrekjafullstack.quiz.repo;

import com.ekosrekja.ekosrekjafullstack.quiz.entity.QuizOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
    List<QuizOption> findByQuestionIdOrderByOrdAsc(Long questionId);
}
