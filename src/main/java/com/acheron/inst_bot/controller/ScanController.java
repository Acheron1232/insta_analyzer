package com.acheron.inst_bot.controller;

import com.acheron.inst_bot.dto.ScanJobDto;
import com.acheron.inst_bot.dto.ScanRequest;
import com.acheron.inst_bot.dto.UkraineScanRequest;
import com.acheron.inst_bot.model.ScanJob;
import com.acheron.inst_bot.repository.ScanJobRepository;
import com.acheron.inst_bot.service.ScanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;
    private final ScanJobRepository scanJobRepo;

    @PostMapping
    public ResponseEntity<ScanJobDto> startScan(@Valid @RequestBody ScanRequest request) {
        ScanJob job = scanService.createScan(request.query(), request.dataSource(), request.aiProvider());
        scanService.executeScan(job.getId(), request.limit());
        return ResponseEntity.ok(ScanJobDto.from(job));
    }

    @PostMapping("/ukraine")
    public ResponseEntity<ScanJobDto> startUkraineScan(@Valid @RequestBody UkraineScanRequest request) {
        String querySummary = "ukraine:auto";
        ScanJob job = scanService.createScan(querySummary, request.dataSource(), request.aiProvider());
        scanService.executeUkraineScan(job.getId(), request.limit(), request.niches(), request.cities());
        return ResponseEntity.ok(ScanJobDto.from(job));
    }

    @GetMapping
    public List<ScanJobDto> listScans() {
        return scanService.getAllScans().stream()
                .map(ScanJobDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScanJobDto> getScan(@PathVariable Long id) {
        return scanJobRepo.findById(id)
                .map(j -> ResponseEntity.ok(ScanJobDto.from(j)))
                .orElse(ResponseEntity.notFound().build());
    }
}
