package com.ekosrekja.ekosrekjafullstack.admin;

import com.ekosrekja.ekosrekjafullstack.fun.entity.Game;
import com.ekosrekja.ekosrekjafullstack.fun.entity.HoroscopeEntry;
import com.ekosrekja.ekosrekjafullstack.fun.entity.Photo;
import com.ekosrekja.ekosrekjafullstack.fun.entity.Video;
import com.ekosrekja.ekosrekjafullstack.fun.repos.GameRepository;
import com.ekosrekja.ekosrekjafullstack.fun.repos.HoroscopeRepository;
import com.ekosrekja.ekosrekjafullstack.fun.repos.PhotoRepository;
import com.ekosrekja.ekosrekjafullstack.fun.repos.VideoRepository;
import com.ekosrekja.ekosrekjafullstack.news.News;
import com.ekosrekja.ekosrekjafullstack.news.NewsRepository;
import com.ekosrekja.ekosrekjafullstack.quiz.entity.Quiz;
import com.ekosrekja.ekosrekjafullstack.quiz.entity.QuizOption;
import com.ekosrekja.ekosrekjafullstack.quiz.entity.QuizQuestion;
import com.ekosrekja.ekosrekjafullstack.quiz.repo.QuizOptionRepository;
import com.ekosrekja.ekosrekjafullstack.quiz.repo.QuizQuestionRepository;
import com.ekosrekja.ekosrekjafullstack.quiz.repo.QuizRepository;
import com.ekosrekja.ekosrekjafullstack.quiz.repo.QuizSubmissionRepository;
import com.ekosrekja.ekosrekjafullstack.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;
    private final PhotoRepository photoRepository;
    private final VideoRepository videoRepository;
    private final GameRepository gameRepository;
    private final HoroscopeRepository horoscopeRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    @GetMapping("/news")
    public Page<News> news(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin(userId);
        return newsRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PostMapping("/news")
    public ResponseEntity<News> createNews(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody News news) {
        requireAdmin(userId);
        news.setPublished(true);
        if (news.getPublishedAt() == null) {
            news.setPublishedAt(Instant.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newsRepository.save(news));
    }

    @DeleteMapping("/news/{id}")
    public ResponseEntity<Void> deleteNews(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        requireAdmin(userId);
        return deleteById(newsRepository, id);
    }

    @GetMapping("/quizzes")
    public Page<Quiz> quizzes(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin(userId);
        return quizRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "title")));
    }

    @PostMapping("/quizzes")
    @Transactional
    public ResponseEntity<Quiz> createQuiz(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AdminQuizRequest request) {
        requireAdmin(userId);

        Quiz quiz = new Quiz();
        quiz.setTitle(request.title());
        quiz.setDescription(request.description());
        quiz.setLevel(request.level());
        quiz.setTimeMinutes(request.timeMinutes() == null ? 5 : request.timeMinutes());
        quiz.setActive(true);
        Quiz savedQuiz = quizRepository.save(quiz);

        for (AdminQuizQuestionRequest questionRequest : safeList(request.questions())) {
            QuizQuestion question = new QuizQuestion();
            question.setQuiz(savedQuiz);
            question.setText(questionRequest.text());
            QuizQuestion savedQuestion = quizQuestionRepository.save(question);

            int order = 0;
            for (AdminQuizOptionRequest optionRequest : safeList(questionRequest.options())) {
                QuizOption option = new QuizOption();
                option.setQuestion(savedQuestion);
                option.setText(optionRequest.text());
                option.setCorrect(Boolean.TRUE.equals(optionRequest.correct()));
                option.setOrd(order++);
                quizOptionRepository.save(option);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedQuiz);
    }

    @DeleteMapping("/quizzes/{id}")
    @Transactional
    public ResponseEntity<Void> deleteQuiz(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        requireAdmin(userId);
        if (!quizRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        var questions = quizQuestionRepository.findByQuizIdOrderByIdAsc(id);
        for (QuizQuestion question : questions) {
            quizOptionRepository.deleteByQuestionId(question.getId());
        }
        quizSubmissionRepository.deleteByQuizId(id);
        quizQuestionRepository.deleteByQuizId(id);
        quizRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/photos")
    public Page<Photo> photos(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin(userId);
        return photoRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PostMapping("/photos")
    public ResponseEntity<Photo> createPhoto(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Photo photo) {
        requireAdmin(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(photoRepository.save(photo));
    }

    @DeleteMapping("/photos/{id}")
    public ResponseEntity<Void> deletePhoto(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        requireAdmin(userId);
        return deleteById(photoRepository, id);
    }

    @GetMapping("/videos")
    public Page<Video> videos(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin(userId);
        return videoRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PostMapping("/videos")
    public ResponseEntity<Video> createVideo(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Video video) {
        requireAdmin(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(videoRepository.save(video));
    }

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<Void> deleteVideo(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        requireAdmin(userId);
        return deleteById(videoRepository, id);
    }

    @GetMapping("/games")
    public Page<Game> games(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin(userId);
        return gameRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "title")));
    }

    @PostMapping("/games")
    public ResponseEntity<Game> createGame(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Game game) {
        requireAdmin(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameRepository.save(game));
    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<Void> deleteGame(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        requireAdmin(userId);
        return deleteById(gameRepository, id);
    }

    @GetMapping("/horoscope")
    public Page<HoroscopeEntry> horoscope(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin(userId);
        return horoscopeRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PostMapping("/horoscope")
    public ResponseEntity<HoroscopeEntry> createHoroscope(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody HoroscopeEntry entry) {
        requireAdmin(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(horoscopeRepository.save(entry));
    }

    @DeleteMapping("/horoscope/{id}")
    public ResponseEntity<Void> deleteHoroscope(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        requireAdmin(userId);
        return deleteById(horoscopeRepository, id);
    }

    private void requireAdmin(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (!Boolean.TRUE.equals(user.getIsAdmin()) && !adminRepository.existsByUserId(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    private <T, ID> ResponseEntity<Void> deleteById(
            org.springframework.data.jpa.repository.JpaRepository<T, ID> repository,
            ID id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record AdminQuizRequest(
            String title,
            String description,
            String level,
            Integer timeMinutes,
            List<AdminQuizQuestionRequest> questions) {}

    public record AdminQuizQuestionRequest(
            String text,
            List<AdminQuizOptionRequest> options) {}

    public record AdminQuizOptionRequest(
            String text,
            Boolean correct) {}

    private static <T> List<T> safeList(List<T> items) {
        return items == null ? Collections.emptyList() : items;
    }
}
