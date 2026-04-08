package vn.bxh.jobhunter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.SavedJob;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.SavedJobRepository;
import vn.bxh.jobhunter.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public java.util.List<vn.bxh.jobhunter.domain.response.ResSavedJobDTO> getSavedJobsDTO(String email) {
        User user = userRepository.findByEmail(email);
        List<SavedJob> list = savedJobRepository.findByUserId(user.getId());
        return list.stream().map(this::convertToDTO).collect(java.util.stream.Collectors.toList());
    }

    private vn.bxh.jobhunter.domain.response.ResSavedJobDTO convertToDTO(SavedJob savedJob) {
        vn.bxh.jobhunter.domain.response.ResSavedJobDTO dto = new vn.bxh.jobhunter.domain.response.ResSavedJobDTO();
        dto.setId(savedJob.getId());
        dto.setCreatedAt(savedJob.getCreatedAt());

        if (savedJob.getJob() != null) {
            Job job = savedJob.getJob();
            vn.bxh.jobhunter.domain.response.ResSavedJobDTO.JobSaved jobSaved = 
                new vn.bxh.jobhunter.domain.response.ResSavedJobDTO.JobSaved();
            jobSaved.setId(job.getId());
            jobSaved.setName(job.getName());
            jobSaved.setLocation(job.getLocation());
            jobSaved.setSalary(job.getSalary());

            if (job.getCompany() != null) {
                Company company = job.getCompany();
                vn.bxh.jobhunter.domain.response.ResSavedJobDTO.CompanySaved companySaved = 
                    new vn.bxh.jobhunter.domain.response.ResSavedJobDTO.CompanySaved();
                companySaved.setId(company.getId());
                companySaved.setName(company.getName());
                companySaved.setLogo(company.getLogo());
                jobSaved.setCompany(companySaved);
            }
            dto.setJob(jobSaved);
        }
        return dto;
    }

    public boolean isSaved(String email, long jobId) {
        User user = userRepository.findByEmail(email);
        return savedJobRepository.existsByUserIdAndJobId(user.getId(), jobId);
    }

    public SavedJob save(String email, long jobId) {
        User user = userRepository.findByEmail(email);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        if (savedJobRepository.existsByUserIdAndJobId(user.getId(), jobId)) {
            return savedJobRepository.findByUserIdAndJobId(user.getId(), jobId).get();
        }
        SavedJob savedJob = new SavedJob();
        savedJob.setUser(user);
        savedJob.setJob(job);
        return savedJobRepository.save(savedJob);
    }

    @Transactional
    public void unsave(String email, long jobId) {
        User user = userRepository.findByEmail(email);
        savedJobRepository.deleteByUserIdAndJobId(user.getId(), jobId);
    }
}
