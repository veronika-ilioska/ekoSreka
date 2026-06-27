package com.ekosrekja.ekosrekjafullstack.quiz.entity;

import com.ekosrekja.ekosrekjafullstack.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Getter
@Setter
public class QuizSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Instant createdAt = Instant.now();
    private Integer score;
    private Integer total;
}


