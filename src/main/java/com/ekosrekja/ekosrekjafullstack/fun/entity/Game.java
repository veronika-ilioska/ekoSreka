package com.ekosrekja.ekosrekjafullstack.fun.entity;

import com.ekosrekja.ekosrekjafullstack.fun.Difficulty;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "games")
@Getter
@Setter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 3000)
    private String description;

    @Column(length = 4000)
    private String rules;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private String thumbnailUrl;
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PrePersist
    @PreUpdate
    protected void touchUpdatedAt() {
        updatedAt = OffsetDateTime.now();
    }
}


