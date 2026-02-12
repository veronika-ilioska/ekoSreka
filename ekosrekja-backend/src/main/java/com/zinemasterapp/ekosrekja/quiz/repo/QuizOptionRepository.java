package com.zinemasterapp.ekosrekja.quiz.repo;

import com.zinemasterapp.ekosrekja.quiz.entity.QuizOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
    List<QuizOption> findByQuestionIdOrderByOrdAsc(Long questionId);
}
