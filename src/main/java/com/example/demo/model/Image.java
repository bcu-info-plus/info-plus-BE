package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int imageId;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;

    @Column(nullable = false, length = 255)
    private String imageUrl;
}
