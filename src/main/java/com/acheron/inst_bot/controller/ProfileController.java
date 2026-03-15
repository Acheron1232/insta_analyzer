package com.acheron.inst_bot.controller;

import com.acheron.inst_bot.config.AnalysisConfig;
import com.acheron.inst_bot.dto.AnalyzeRequest;
import com.acheron.inst_bot.dto.ProfileDto;
import com.acheron.inst_bot.model.InstagramProfile;
import com.acheron.inst_bot.repository.InstagramProfileRepository;
import com.acheron.inst_bot.service.ProfileAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final InstagramProfileRepository profileRepo;
    private final ProfileAnalysisService analysisService;
    private final AnalysisConfig analysisConfig;

    @GetMapping
    public Page<ProfileDto> listProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return profileRepo.findAllRanked(PageRequest.of(page, size))
                .map(ProfileDto::from);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable Long id) {
        return profileRepo.findById(id)
                .map(p -> ResponseEntity.ok(ProfileDto.from(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ranked")
    public List<ProfileDto> getRanked() {
        return profileRepo.findAllRanked()
                .stream()
                .map(ProfileDto::from)
                .toList();
    }

    @GetMapping("/leads")
    public List<ProfileDto> getQualifiedLeads() {
        return profileRepo.findQualifiedLeads(analysisConfig.leadScoreThreshold())
                .stream()
                .map(ProfileDto::from)
                .toList();
    }

    @PostMapping("/analyze")
    public ResponseEntity<ProfileDto> analyzeProfile(@Valid @RequestBody AnalyzeRequest request) {
        try {
            InstagramProfile result = analysisService.analyzeProfile(
                    request.username(), request.dataSource(), request.aiProvider());
            return ResponseEntity.ok(ProfileDto.from(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
