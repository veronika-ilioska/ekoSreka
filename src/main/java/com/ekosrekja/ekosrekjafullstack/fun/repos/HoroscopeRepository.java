package com.ekosrekja.ekosrekjafullstack.fun.repos;

import com.ekosrekja.ekosrekjafullstack.fun.PeriodType;
import com.ekosrekja.ekosrekjafullstack.fun.ZodiacSign;
import com.ekosrekja.ekosrekjafullstack.fun.entity.HoroscopeEntry;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoroscopeRepository extends JpaRepository<HoroscopeEntry, Long> {
    Optional<HoroscopeEntry> findTopBySignOrderByPeriodDateDesc(ZodiacSign sign);

    Optional<HoroscopeEntry> findBySignAndPeriodTypeAndPeriodDate(
            ZodiacSign s, PeriodType t, LocalDate d);
}


