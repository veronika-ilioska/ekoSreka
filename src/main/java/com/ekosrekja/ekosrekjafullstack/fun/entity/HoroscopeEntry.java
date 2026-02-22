package com.ekosrekja.ekosrekjafullstack.fun.entity;

import com.ekosrekja.ekosrekjafullstack.fun.PeriodType;
import com.ekosrekja.ekosrekjafullstack.fun.ZodiacSign;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "horoscope_entries")
@Getter
@Setter
public class HoroscopeEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ZodiacSign sign;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private LocalDate periodDate;

    private String title;

    @Column(length = 4000)
    private String content;

    private String ecoTip;
}


