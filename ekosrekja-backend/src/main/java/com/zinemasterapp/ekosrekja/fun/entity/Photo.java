package com.zinemasterapp.ekosrekja.fun.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "photos")
@Setter
@Getter
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(length = 2000) private String description;
    private String url;
    private String thumbnailUrl;

    private String tags; //dali mi treba??

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
