package vn.bxh.jobhunter.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.Skill;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Optional<Job> findByName(String name);
    List<Job> findBySkillsIn(List<Skill> skillList);

}
