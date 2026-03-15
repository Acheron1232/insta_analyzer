package com.acheron.inst_bot.model;

import com.acheron.inst_bot.model.enums.AiProvider;
import com.acheron.inst_bot.model.enums.DataSource;
import com.acheron.inst_bot.model.enums.ScanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scan_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String searchQuery;

    @Enumerated(EnumType.STRING)
    private DataSource dataSource;

    @Enumerated(EnumType.STRING)
    private AiProvider aiProvider;

    @Enumerated(EnumType.STRING)
    private ScanStatus status;

    private int profilesFound;
    private int profilesAnalyzed;
    private int leadsFound;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Column(length = 2000)
    private String errorMessage;
}
