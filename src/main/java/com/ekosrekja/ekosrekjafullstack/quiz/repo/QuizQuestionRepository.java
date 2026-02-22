package com.ekosrekja.ekosrekjafullstack.quiz.repo;

import com.ekosrekja.ekosrekjafullstack.quiz.entity.QuizQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizIdOrderByIdAsc(Long quizId);
}


