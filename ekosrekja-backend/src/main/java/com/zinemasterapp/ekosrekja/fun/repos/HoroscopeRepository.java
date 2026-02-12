package com.zinemasterapp.ekosrekja.fun.repos;

import com.zinemasterapp.ekosrekja.fun.PeriodType;
import com.zinemasterapp.ekosrekja.fun.ZodiacSign;
import com.zinemasterapp.ekosrekja.fun.entity.HoroscopeEntry;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoroscopeRepository extends JpaRepository<HoroscopeEntry, Long> {
    Optional<HoroscopeEntry> findTopBySignOrderByPeriodDateDesc(ZodiacSign sign);

    Optional<HoroscopeEntry> findBySignAndPeriodTypeAndPeriodDate(
            ZodiacSign s, PeriodType t, LocalDate d);
}
