package com.ekosrekja.ekosrekjafullstack.fun;

import java.time.Instant;
import java.time.LocalDate;

public class dto {
    public record HoroscopeDto(
            Long id,
            ZodiacSign sign,
            PeriodType periodType,
            LocalDate periodDate,
            String title,
            String content,
            String ecoTip,
            Instant createdAt) {}

    public record CountDto(long count) {}
}


