package com.servio.backend.identity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_logo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String fullPath;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;
}