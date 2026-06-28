package com.ekosrekja.ekosrekjafullstack.admin;

import com.ekosrekja.ekosrekjafullstack.fun.Difficulty;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    private final ObjectMapper objectMapper;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/news")
    public Page<News> news(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        requireAdmin(userId);
        return newsRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PostMapping(value = "/news", consumes = MediaType.APPLICATION_JSON_VALUE)
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

    @PostMapping(value = "/news", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<News> uploadNews(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam String content,
            @RequestParam("file") MultipartFile file) {
        requireAdmin(userId);
        String mediaUrl = storeUpload(file, "news", "image/");

        News news = new News();
        news.setTitle(title);
        news.setCategory(category);
        news.setContent(content);
        news.setCoverUrl(mediaUrl);
        news.setPublished(true);
        news.setPublishedAt(Instant.now());
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

    @PostMapping(value = "/quizzes", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Quiz> createQuiz(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AdminQuizRequest request) {
        requireAdmin(userId);

        return createQuizFromRequest(request, null);
    }

    @PostMapping(value = "/quizzes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Quiz> uploadQuiz(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String title,
            @RequestParam String level,
            @RequestParam(required = false) Integer timeMinutes,
            @RequestParam String description,
            @RequestParam String questions,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        requireAdmin(userId);

        List<AdminQuizQuestionRequest> questionRequests;
        try {
            questionRequests = objectMapper.readValue(
                    questions,
                    new TypeReference<List<AdminQuizQuestionRequest>>() {});
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quiz questions", ex);
        }

        String imageUrl = file == null || file.isEmpty() ? null : storeUpload(file, "quizzes", "image/");
        return createQuizFromRequest(
                new AdminQuizRequest(title, description, level, timeMinutes, questionRequests),
                imageUrl);
    }

    private ResponseEntity<Quiz> createQuizFromRequest(AdminQuizRequest request, String imageUrl) {
        Quiz quiz = new Quiz();
        quiz.setTitle(request.title());
        quiz.setDescription(request.description());
        quiz.setLevel(request.level());
        quiz.setImageUrl(imageUrl);
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

    @PostMapping(value = "/photos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Photo> createPhoto(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Photo photo) {
        requireAdmin(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(photoRepository.save(photo));
    }

    @PostMapping(value = "/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Photo> uploadPhoto(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String tags,
            @RequestParam("file") MultipartFile file) {
        requireAdmin(userId);
        String mediaUrl = storeUpload(file, "photos", "image/");

        Photo photo = new Photo();
        photo.setTitle(title);
        photo.setDescription(description);
        photo.setTags(tags);
        photo.setUrl(mediaUrl);
        photo.setThumbnailUrl(mediaUrl);
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

    @PostMapping(value = "/videos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Video> createVideo(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Video video) {
        requireAdmin(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(videoRepository.save(video));
    }

    @PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Video> uploadVideo(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) Integer durationSec,
            @RequestParam("file") MultipartFile file) {
        requireAdmin(userId);
        String mediaUrl = storeUpload(file, "videos", "video/");

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setSource("UPLOAD");
        video.setIcon(icon);
        video.setRef(mediaUrl);
        video.setDurationSec(durationSec);
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

    @PostMapping(value = "/games", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Game> createGame(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Game game) {
        requireAdmin(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(gameRepository.save(game));
    }

    @PostMapping(value = "/games", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Game> uploadGame(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String title,
            @RequestParam Difficulty difficulty,
            @RequestParam String description,
            @RequestParam(required = false) String rules,
            @RequestParam("file") MultipartFile file) {
        requireAdmin(userId);
        String mediaUrl = storeUpload(file, "games", "image/");

        Game game = new Game();
        game.setTitle(title);
        game.setDifficulty(difficulty);
        game.setDescription(description);
        game.setRules(rules);
        game.setThumbnailUrl(mediaUrl);
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

    private String storeUpload(MultipartFile file, String folder, String expectedContentTypePrefix) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload file is required");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith(expectedContentTypePrefix)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type");
        }

        String extension = extensionFrom(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        Path targetFolder = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(folder);
        Path target = targetFolder.resolve(filename).normalize();
        if (!target.startsWith(targetFolder)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        try {
            Files.createDirectories(targetFolder);
            file.transferTo(target);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save uploaded file", ex);
        }

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(folder)
                .path("/")
                .path(filename)
                .toUriString();
    }

    private String extensionFrom(String filename) {
        if (filename == null) return "";
        String cleanName = Paths.get(filename).getFileName().toString();
        int dotIndex = cleanName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleanName.length() - 1) return "";
        return cleanName.substring(dotIndex).toLowerCase(Locale.ROOT);
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
