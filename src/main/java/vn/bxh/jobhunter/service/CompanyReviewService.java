package vn.bxh.jobhunter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.CompanyReview;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.repository.CompanyRepository;
import vn.bxh.jobhunter.repository.CompanyReviewRepository;
import vn.bxh.jobhunter.repository.UserRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompanyReviewService {

    private final CompanyReviewRepository reviewRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public List<CompanyReview> getReviews(long companyId) {
        return reviewRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
    }

    public Map<String, Object> getSummary(long companyId) {
        Double avg = reviewRepository.avgRatingByCompanyId(companyId);
        long count = reviewRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).size();
        return Map.of(
                "averageRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0,
                "totalReviews", count
        );
    }

    public CompanyReview create(String email, long companyId, int rating, String comment) {
        User user = userRepository.findByEmail(email);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        CompanyReview review = new CompanyReview();
        review.setUser(user);
        review.setCompany(company);
        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    public boolean hasReviewed(String email, long companyId) {
        User user = userRepository.findByEmail(email);
        return reviewRepository.existsByUserIdAndCompanyId(user.getId(), companyId);
    }
}
