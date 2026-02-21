package com.ekosrekja.ekosrekjafullstack.fun.controllers;

import com.zinemasterapp.ekosrekja.fun.dto;
import com.zinemasterapp.ekosrekja.fun.repos.GameRepository;
import com.zinemasterapp.ekosrekja.fun.repos.HoroscopeRepository;
import com.zinemasterapp.ekosrekja.fun.repos.PhotoRepository;
import com.zinemasterapp.ekosrekja.fun.repos.VideoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CountController {
    private final PhotoRepository photos;
    private final VideoRepository videos;
    private final HoroscopeRepository horo;
    private final GameRepository games;

    public CountController(
            PhotoRepository photos,
            VideoRepository videos,
            HoroscopeRepository horo,
            GameRepository games) {
        this.photos = photos;
        this.videos = videos;
        this.horo = horo;
        this.games = games;
    }

    @GetMapping("/media/photos/count")
    public dto.CountDto photosCount() {
        return new dto.CountDto(photos.count());
    }

    @GetMapping("/media/videos/count")
    public dto.CountDto videosCount() {
        return new dto.CountDto(videos.count());
    }

    @GetMapping("/horoscope/count")
    public dto.CountDto horoCount() {
        return new dto.CountDto(horo.count());
    }

    @GetMapping("/games/count")
    public dto.CountDto gamesCount() {
        return new dto.CountDto(games.count());
    }
}
