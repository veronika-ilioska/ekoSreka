package com.zinemasterapp.ekosrekja.fun.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "photos")
@Setter
@Getter
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    private String url;
    private String thumbnailUrl;

    private String tags; // dali mi treba??

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
