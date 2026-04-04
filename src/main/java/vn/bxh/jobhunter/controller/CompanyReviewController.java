package vn.bxh.jobhunter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.bxh.jobhunter.domain.CompanyReview;
import vn.bxh.jobhunter.service.CompanyReviewService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/reviews")
@RequiredArgsConstructor
public class CompanyReviewController {

    private final CompanyReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<CompanyReview>> getReviews(@PathVariable long companyId) {
        return ResponseEntity.ok(reviewService.getReviews(companyId));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable long companyId) {
        return ResponseEntity.ok(reviewService.getSummary(companyId));
    }

    @PostMapping
    public ResponseEntity<CompanyReview> createReview(
            @PathVariable long companyId,
            @RequestBody Map<String, Object> body,
            Principal principal) {
        int rating = Integer.parseInt(body.get("rating").toString());
        String comment = body.getOrDefault("comment", "").toString();
        return ResponseEntity.ok(reviewService.create(principal.getName(), companyId, rating, comment));
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Boolean>> hasReviewed(@PathVariable long companyId, Principal principal) {
        return ResponseEntity.ok(Map.of("reviewed", reviewService.hasReviewed(principal.getName(), companyId)));
    }
}
