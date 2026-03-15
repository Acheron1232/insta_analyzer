package com.acheron.inst_bot.repository;

import com.acheron.inst_bot.model.ScanJob;
import com.acheron.inst_bot.model.enums.ScanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScanJobRepository extends JpaRepository<ScanJob, Long> {

    List<ScanJob> findByStatusOrderByStartedAtDesc(ScanStatus status);

    List<ScanJob> findAllByOrderByStartedAtDesc();
}
