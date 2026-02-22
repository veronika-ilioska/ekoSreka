package com.ekosrekja.ekosrekjafullstack.news;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 8000)
    private String content;

    private String category;
    private String coverUrl;
    private Instant publishedAt;
    private boolean published;
}


