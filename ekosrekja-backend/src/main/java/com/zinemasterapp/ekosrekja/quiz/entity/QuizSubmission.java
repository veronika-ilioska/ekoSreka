package com.zinemasterapp.ekosrekja.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
public class QuizSubmission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="quiz_id")
    private Quiz quiz;
    private Instant createdAt = Instant.now();
    private Integer score;
    private Integer total;

}
