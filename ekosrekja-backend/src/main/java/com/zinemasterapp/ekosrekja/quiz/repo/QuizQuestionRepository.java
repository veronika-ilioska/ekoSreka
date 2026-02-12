package com.zinemasterapp.ekosrekja.quiz.repo;

import com.zinemasterapp.ekosrekja.quiz.entity.QuizQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizIdOrderByIdAsc(Long quizId);
}
