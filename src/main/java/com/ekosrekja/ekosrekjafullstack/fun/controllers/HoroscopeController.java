package com.ekosrekja.ekosrekjafullstack.fun.controllers;

import com.ekosrekja.ekosrekjafullstack.fun.PeriodType;
import com.ekosrekja.ekosrekjafullstack.fun.ZodiacSign;
import com.ekosrekja.ekosrekjafullstack.fun.dto;
import com.ekosrekja.ekosrekjafullstack.fun.repos.HoroscopeRepository;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/horoscope")
public class HoroscopeController {
    private final HoroscopeRepository repo;

    public HoroscopeController(HoroscopeRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public Page<dto.HoroscopeDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return repo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toDto);
    }

    // /api/horoscope/latest?sign=ARIES
    @GetMapping("/latest")
    public dto.HoroscopeDto latestBySign(@RequestParam ZodiacSign sign) {
        var e =
                repo.findTopBySignOrderByPeriodDateDesc(sign)
                        .orElseThrow(() -> new RuntimeException("No entries"));
        return toDto(e);
    }

    // /api/horoscope/by-period?sign=ARIES&periodType=DAILY&date=2025-10-06
    @GetMapping("/by-period")
    public dto.HoroscopeDto byPeriod(
            @RequestParam ZodiacSign sign,
            @RequestParam PeriodType periodType,
            @RequestParam LocalDate date) {
        var e =
                repo.findBySignAndPeriodTypeAndPeriodDate(sign, periodType, date)
                        .orElseThrow(() -> new RuntimeException("Not found"));
        return toDto(e);
    }

    private dto.HoroscopeDto toDto(com.ekosrekja.ekosrekjafullstack.fun.entity.HoroscopeEntry e) {
        return new dto.HoroscopeDto(
                e.getId(),
                e.getSign(),
                e.getPeriodType(),
                e.getPeriodDate(),
                e.getTitle(),
                e.getContent(),
                e.getEcoTip(),
                e.getCreatedAt());
    }
}


