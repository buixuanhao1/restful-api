package vn.bxh.jobhunter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.SavedJob;
import vn.bxh.jobhunter.service.SavedJobService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobService savedJobService;

    @GetMapping
    public ResponseEntity<List<vn.bxh.jobhunter.domain.response.ResSavedJobDTO>> getSavedJobs(Principal principal) {
        return ResponseEntity.ok(savedJobService.getSavedJobsDTO(principal.getName()));
    }

    @GetMapping("/{jobId}/check")
    public ResponseEntity<Map<String, Boolean>> checkSaved(@PathVariable long jobId, Principal principal) {
        boolean saved = savedJobService.isSaved(principal.getName(), jobId);
        return ResponseEntity.ok(Map.of("saved", saved));
    }

    @PostMapping("/{jobId}")
    public ResponseEntity<SavedJob> saveJob(@PathVariable long jobId, Principal principal) {
        return ResponseEntity.ok(savedJobService.save(principal.getName(), jobId));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> unsaveJob(@PathVariable long jobId, Principal principal) {
        savedJobService.unsave(principal.getName(), jobId);
        return ResponseEntity.noContent().build();
    }
}
