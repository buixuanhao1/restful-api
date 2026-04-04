package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.bxh.jobhunter.domain.CompanyReview;

import java.util.List;

@Repository
public interface CompanyReviewRepository extends JpaRepository<CompanyReview, Long> {
    List<CompanyReview> findByCompanyIdOrderByCreatedAtDesc(long companyId);

    @Query("SELECT AVG(r.rating) FROM CompanyReview r WHERE r.company.id = :companyId")
    Double avgRatingByCompanyId(@Param("companyId") long companyId);

    boolean existsByUserIdAndCompanyId(long userId, long companyId);
}
