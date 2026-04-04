package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.bxh.jobhunter.domain.SavedJob;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByUserId(long userId);
    Optional<SavedJob> findByUserIdAndJobId(long userId, long jobId);
    boolean existsByUserIdAndJobId(long userId, long jobId);
    void deleteByUserIdAndJobId(long userId, long jobId);
}
