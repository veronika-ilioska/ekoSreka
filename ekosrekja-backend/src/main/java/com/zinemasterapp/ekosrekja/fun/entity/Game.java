package com.zinemasterapp.ekosrekja.fun.entity;

import com.zinemasterapp.ekosrekja.fun.Difficulty;
import jakarta.persistence.*;

@Entity @Table(name="games")
public class Game {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(length = 3000) private String description;
    @Column(length = 4000) private String rules;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private String thumbnailUrl;

}
