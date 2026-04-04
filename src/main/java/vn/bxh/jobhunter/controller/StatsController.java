package vn.bxh.jobhunter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.bxh.jobhunter.repository.*;
import vn.bxh.jobhunter.util.Constant.ResumeStateEnum;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final ResumeRepository resumeRepository;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Long>> getOverview() {
        return ResponseEntity.ok(Map.of(
                "totalUsers", userRepository.count(),
                "totalJobs", jobRepository.count(),
                "totalCompanies", companyRepository.count(),
                "totalResumes", resumeRepository.count()
        ));
    }

    @GetMapping("/resumes-by-status")
    public ResponseEntity<List<Map<String, Object>>> getResumesByStatus() {
        List<Map<String, Object>> result = List.of(
                Map.of("status", "PENDING",   "count", resumeRepository.countByStatus(ResumeStateEnum.PENDING)),
                Map.of("status", "REVIEWING", "count", resumeRepository.countByStatus(ResumeStateEnum.REVIEWING)),
                Map.of("status", "APPROVED",  "count", resumeRepository.countByStatus(ResumeStateEnum.APPROVED)),
                Map.of("status", "REJECTED",  "count", resumeRepository.countByStatus(ResumeStateEnum.REJECTED))
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top-companies")
    public ResponseEntity<List<Map<String, Object>>> getTopCompanies() {
        return ResponseEntity.ok(resumeRepository.countResumesByCompany());
    }

    @GetMapping("/jobs-by-level")
    public ResponseEntity<List<Map<String, Object>>> getJobsByLevel() {
        return ResponseEntity.ok(jobRepository.countByLevel());
    }
}
