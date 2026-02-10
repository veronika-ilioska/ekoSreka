package com.zinemasterapp.ekosrekja.fun.controllers;


import com.zinemasterapp.ekosrekja.fun.Difficulty;
import com.zinemasterapp.ekosrekja.fun.entity.Photo;
import com.zinemasterapp.ekosrekja.fun.entity.Video;
import com.zinemasterapp.ekosrekja.fun.repos.GameRepository;
import com.zinemasterapp.ekosrekja.fun.repos.PhotoRepository;
import com.zinemasterapp.ekosrekja.fun.repos.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ListingController {
    private final PhotoRepository photoRepo;
    private final VideoRepository videoRepo;
    private final GameRepository gameRepo;

    public ListingController(PhotoRepository p, VideoRepository v, GameRepository g) {
        this.photoRepo = p; this.videoRepo = v; this.gameRepo = g;
    }

    @GetMapping("/media/photos")
    public Page<Photo> photos(@RequestParam(defaultValue = "") String q,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "12") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return q.isBlank() ? photoRepo.findAll(pageable)
                : photoRepo.findByTitleContainingIgnoreCase(q, pageable);
    }

    @GetMapping("/media/videos")
    public Page<Video> videos(@RequestParam(defaultValue = "") String q,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "12") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return q.isBlank() ? videoRepo.findAll(pageable)
                : videoRepo.findByTitleContainingIgnoreCase(q, pageable);
    }

    @GetMapping("/games")
    public Page<com.zinemasterapp.ekosrekja.fun.entity.Game> games(@RequestParam(defaultValue = "") String q,
                                                      @RequestParam(required = false) Difficulty difficulty,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "12") int size) {
        var pageable = PageRequest.of(page, size);
        if (difficulty != null) return gameRepo.findByDifficulty(difficulty, pageable);
        return q.isBlank() ? gameRepo.findAll(pageable)
                : gameRepo.findByTitleContainingIgnoreCase(q, pageable);
    }
}
