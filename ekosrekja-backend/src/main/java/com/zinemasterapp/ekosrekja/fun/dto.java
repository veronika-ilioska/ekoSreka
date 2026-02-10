package com.zinemasterapp.ekosrekja.fun;

import java.time.LocalDate;

public class dto {
    public record HoroscopeDto(
            Long id, ZodiacSign sign, PeriodType periodType, LocalDate periodDate,
            String title, String content, String ecoTip
    ) {}
    public record CountDto(long count) {}
}
