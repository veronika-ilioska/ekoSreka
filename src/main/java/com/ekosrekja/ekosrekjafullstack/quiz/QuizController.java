package com.ekosrekja.ekosrekjafullstack.quiz;

import com.ekosrekja.ekosrekjafullstack.quiz.entity.Quiz;
import com.ekosrekja.ekosrekjafullstack.quiz.entity.QuizOption;
import com.ekosrekja.ekosrekjafullstack.quiz.entity.QuizQuestion;
import com.ekosrekja.ekosrekjafullstack.quiz.entity.QuizSubmission;
import com.ekosrekja.ekosrekjafullstack.quiz.repo.QuizOptionRepository;
import com.ekosrekja.ekosrekjafullstack.quiz.repo.QuizQuestionRepository;
import com.ekosrekja.ekosrekjafullstack.quiz.repo.QuizRepository;
import com.ekosrekja.ekosrekjafullstack.quiz.repo.QuizSubmissionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {
    private final QuizRepository quizRepo;
    private final QuizQuestionRepository qqRepo;
    private final QuizOptionRepository qoRepo;
    private final QuizSubmissionRepository subRepo;

    @GetMapping
    public Page<dto.QuizListItem> list(
            @RequestParam(required = false) String level, // ako nema level site kvizovi gi davame
            @PageableDefault(size = 12) Pageable p) {
        Page<Quiz> page =
                (level == null || level.isBlank())
                        ? quizRepo.findByActiveTrue(p)
                        : quizRepo.findByActiveTrueAndLevelIgnoreCase(level, p);

        return page.map(
                q ->
                        new dto.QuizListItem(
                                q.getId(),
                                q.getTitle(),
                                q.getLevel(),
                                q.getDescription(),
                                q.getTimeMinutes()));
    }

    @GetMapping("/{id}")
    public Map<String, Object> getQuiz(@PathVariable Long id) {
        Quiz quiz = quizRepo.findById(id).orElseThrow();
        List<QuizQuestion> questions = qqRepo.findByQuizIdOrderByIdAsc(id);

        List<dto.QuizQuestionDto> qdto =
                questions.stream()
                        .map(
                                q -> {
                                    List<QuizOption> opts =
                                            qoRepo.findByQuestionIdOrderByOrdAsc(
                                                    q.getId()); // gi barame options na prashanjata
                                    var optDtos =
                                            opts.stream()
                                                    .map(
                                                            o ->
                                                                    new dto.QuizQuestionDto
                                                                            .OptionDto(
                                                                            o.getId(),
                                                                            o.getText(),
                                                                            o.getOrd()))
                                                    .toList();
                                    return new dto.QuizQuestionDto(q.getId(), q.getText(), optDtos);
                                })
                        .toList();

        return Map.of(
                "id", quiz.getId(),
                "title", quiz.getTitle(),
                "level", quiz.getLevel(),
                "description", quiz.getDescription(),
                "timeMinutes", quiz.getTimeMinutes(),
                "questions", qdto);
    }

    public record AnswerIn(Long questionId, Long optionId) {}

    @PostMapping("/{id}/submit") // id to na kvizot i odgovorite
    public dto.QuizResultDto submit(@PathVariable Long id, @RequestBody List<AnswerIn> answers) {

        List<QuizQuestion> questions =
                qqRepo.findByQuizIdOrderByIdAsc(id); // koj kviz(gi zemame negovite prashanja)
        Map<Long, List<QuizOption>> optionsByQ =
                questions.stream() // za site prashanja gi zemame options
                        .collect(
                                Collectors.toMap(
                                        QuizQuestion::getId,
                                        q ->
                                                qoRepo.findByQuestionIdOrderByOrdAsc(
                                                        q.getId()))); // {id:List<Options>}

        int score = 0;
        List<dto.QuizResultDto.QuestionResult> details = new ArrayList<>();

        for (QuizQuestion q : questions) {
            Long qid = q.getId();
            Long chosen =
                    answers.stream()
                            .filter(a -> a.questionId.equals(qid))
                            .map(a -> a.optionId)
                            .findFirst()
                            .orElse(null);

            Long correct =
                    optionsByQ.get(qid).stream()
                            .filter(option -> option.isCorrect())
                            .map(option -> option.getId())
                            .findFirst()
                            .orElse(null);

            boolean ok = (chosen != null && chosen.equals(correct));
            if (ok) score++; // klk poeni ima

            details.add(new dto.QuizResultDto.QuestionResult(qid, chosen, correct, ok));
        }

        Quiz quiz = quizRepo.findById(id).orElseThrow();
        QuizSubmission sub = new QuizSubmission();
        sub.setQuiz(quiz);
        sub.setScore(score);
        sub.setTotal(questions.size());
        subRepo.save(sub);

        return new dto.QuizResultDto(score, questions.size(), details);
    }
}


