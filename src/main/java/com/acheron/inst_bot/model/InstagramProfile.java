package com.acheron.inst_bot.model;

import com.acheron.inst_bot.model.enums.DataSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "instagram_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstagramProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String fullName;

    @Column(length = 2000)
    private String biography;

    private String externalUrl;
    private String profilePicUrl;
    private int followerCount;
    private int followingCount;
    private int mediaCount;
    private boolean businessAccount;
    private boolean privateAccount;
    private boolean verified;
    private String category;
    private String websiteDomain;
    private String contactPhone;
    private String telegramHandle;

    @Column(length = 512)
    private String dedupeKey;

    @Enumerated(EnumType.STRING)
    private DataSource dataSource;

    private LocalDateTime fetchedAt;
    private LocalDateTime lastPostAt;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AnalysisResult analysisResult;
}
