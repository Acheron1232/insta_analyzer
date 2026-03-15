package com.acheron.inst_bot.model;

import com.acheron.inst_bot.model.enums.AiProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "profile_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InstagramProfile profile;

    private int overallScore;
    private boolean smallBusiness;
    private boolean ukraineBased;
    private boolean lacksWebsite;
    private boolean lacksCrm;
    private boolean lacksTelegramBot;
    private boolean sellsViaDms;
    private boolean active;
    private String detectedCategory;

    @Column(length = 2000)
    private String reasoning;

    private Integer rankPosition;

    @Enumerated(EnumType.STRING)
    private AiProvider aiProvider;

    private LocalDateTime analyzedAt;
}
