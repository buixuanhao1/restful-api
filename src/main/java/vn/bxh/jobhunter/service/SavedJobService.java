package vn.bxh.jobhunter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public List<SavedJob> getSavedJobs(String email) {
        User user = userRepository.findByEmail(email);
        return savedJobRepository.findByUserId(user.getId());
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
