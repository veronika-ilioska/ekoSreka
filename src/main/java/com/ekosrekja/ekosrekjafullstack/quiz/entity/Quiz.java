package com.ekosrekja.ekosrekjafullstack.quiz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String level;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    private Integer timeMinutes = 5;
    private boolean active = true;
}


