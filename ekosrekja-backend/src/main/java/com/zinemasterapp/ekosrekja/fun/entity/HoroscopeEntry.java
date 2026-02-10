package com.zinemasterapp.ekosrekja.fun.entity;

import com.zinemasterapp.ekosrekja.fun.PeriodType;
import com.zinemasterapp.ekosrekja.fun.ZodiacSign;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name="horoscope_entries")
@Getter
@Setter
public class HoroscopeEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ZodiacSign sign;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private LocalDate periodDate;

    private String title;
    @Column(length = 4000) private String content;
    private String ecoTip;


}
