package com.zinemasterapp.ekosrekja.fun.controllers;

import com.zinemasterapp.ekosrekja.fun.PeriodType;
import com.zinemasterapp.ekosrekja.fun.ZodiacSign;
import com.zinemasterapp.ekosrekja.fun.dto;
import com.zinemasterapp.ekosrekja.fun.repos.HoroscopeRepository;
import java.time.LocalDate;
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

    // /api/horoscope/latest?sign=ARIES
    @GetMapping("/latest")
    public dto.HoroscopeDto latestBySign(@RequestParam ZodiacSign sign) {
        var e =
                repo.findTopBySignOrderByPeriodDateDesc(sign)
                        .orElseThrow(() -> new RuntimeException("No entries"));
        return new dto.HoroscopeDto(
                e.getId(),
                e.getSign(),
                e.getPeriodType(),
                e.getPeriodDate(),
                e.getTitle(),
                e.getContent(),
                e.getEcoTip());
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
        return new dto.HoroscopeDto(
                e.getId(),
                e.getSign(),
                e.getPeriodType(),
                e.getPeriodDate(),
                e.getTitle(),
                e.getContent(),
                e.getEcoTip());
    }
}
