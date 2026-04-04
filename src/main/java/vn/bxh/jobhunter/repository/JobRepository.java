package vn.bxh.jobhunter.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.Skill;
import vn.bxh.jobhunter.domain.response.JobWithApplicantCountDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Optional<Job> findByName(String name);
    List<Job> findBySkillsIn(List<Skill> skillList);
    @Query("SELECT new vn.bxh.jobhunter.domain.response.JobWithApplicantCountDTO(j.id, j.name, j.location, j.salary, j.quantity, j.active, COUNT(r)) " +
            "FROM Job j LEFT JOIN j.resumes r " +
            "WHERE j.company.id = :companyId " +
            "GROUP BY j.id, j.name, j.location, j.salary, j.quantity, j.active")
    List<JobWithApplicantCountDTO> findAllWithApplicantCountByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT j.level AS level, COUNT(j) AS count FROM Job j GROUP BY j.level")
    List<Map<String, Object>> countByLevel();
}
