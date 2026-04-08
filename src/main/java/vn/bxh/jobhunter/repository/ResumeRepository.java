package vn.bxh.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.bxh.jobhunter.domain.Resume;
import vn.bxh.jobhunter.util.Constant.ResumeStateEnum;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume,Long>, JpaSpecificationExecutor<Resume> {

    boolean existsByEmail(String email);
    Optional<Resume> findByEmail(String email);

    long countByJobId(Long jobId);

    long countByStatus(ResumeStateEnum status);

    org.springframework.data.domain.Page<Resume> findByUser(vn.bxh.jobhunter.domain.User user, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT r.job.company.name AS company, COUNT(r) AS count " +
           "FROM Resume r GROUP BY r.job.company.name ORDER BY COUNT(r) DESC")
    List<Map<String, Object>> countResumesByCompany();
}
