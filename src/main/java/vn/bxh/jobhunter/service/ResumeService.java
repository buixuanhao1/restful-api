package vn.bxh.jobhunter.service;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Job;
import vn.bxh.jobhunter.domain.Resume;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.domain.response.Resume.ResResumeDTO;
import vn.bxh.jobhunter.repository.JobRepository;
import vn.bxh.jobhunter.repository.ResumeRepository;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ResumeService(ResumeRepository resumeRepository, JobRepository jobRepository,
                         UserRepository userRepository, @Lazy NotificationService notificationService) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public long countResumesByJob(Long jobId) {
        return resumeRepository.countByJobId(jobId);
    }

    public ResultPaginationDTO GetAllUser(Specification<Resume> spec, Pageable pageable){
        Page<Resume> pageResume = this.resumeRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageResume.getNumber()+1, pageResume.getSize(), pageResume.getTotalPages(), pageResume.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        List<ResResumeDTO> resResumeDTOS = new ArrayList<>();
        for (Resume resume : pageResume.getContent()){
            resResumeDTOS.add(this.ConvertToResResumeDTO(resume));
        }
        resultPaginationDTO.setResult(resResumeDTOS);
        return resultPaginationDTO;
    }

    public ResultPaginationDTO GetAllByUser(Pageable pageable){
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User user = this.userRepository.findByEmail(email);
        Page<Resume> pageResume = this.resumeRepository.findByUser(user, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageResume.getNumber()+1, pageResume.getSize(), pageResume.getTotalPages(), pageResume.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        List<ResResumeDTO> resResumeDTOS = new ArrayList<>();
        for (Resume resume : pageResume.getContent()){
            resResumeDTOS.add(this.ConvertToResResumeDTO(resume));
        }
        resultPaginationDTO.setResult(resResumeDTOS);
        return resultPaginationDTO;
    }

    public ResultPaginationDTO GetAllResume(Specification<Resume> spec, Pageable pageable){
        Page<Resume> pageResume = this.resumeRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageResume.getNumber()+1, pageResume.getSize(), pageResume.getTotalPages(), pageResume.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        List<ResResumeDTO> resResumeDTOS = new ArrayList<>();
        for (Resume resume : pageResume.getContent()){
            resResumeDTOS.add(this.ConvertToResResumeDTO(resume));
        }
        resultPaginationDTO.setResult(resResumeDTOS);
        return resultPaginationDTO;
    }

    public ResResumeDTO GetResume(long id){
        Optional<Resume> resumeOptional = this.resumeRepository.findById(id);
        if(resumeOptional.isPresent()){
            return ConvertToResResumeDTO(resumeOptional.get());
        }else{
            throw new IdInvalidException("Id is not valid!");
        }
    }

    public void DeleteResume(long id){
        Optional<Resume> resumeOptional = this.resumeRepository.findById(id);
        if(resumeOptional.isPresent()){
            this.resumeRepository.deleteById(id);
        }else{
            throw new IdInvalidException("Id is not valid!");
        }
    }

    public Resume HandleUpdateResume(Resume resume){
        Optional<Resume> resumeOptional = this.resumeRepository.findById(resume.getId());
        if(resumeOptional.isPresent()){
            Resume resumeUpdate = resumeOptional.get();
            if(resume.getUser()!=null){
                resumeUpdate.setUser(resume.getUser());
            }
            resumeUpdate.setStatus(resume.getStatus());
            Resume saved = this.resumeRepository.save(resumeUpdate);
            // Gửi thông báo cho ứng viên khi trạng thái thay đổi
            if (saved.getUser() != null && saved.getJob() != null) {
                notificationService.notifyResumeStatusChange(
                    saved.getUser(), saved.getJob().getName(), saved.getStatus().name());
            }
            return saved;
        }else{
            throw new IdInvalidException("Id is not valid!");
        }
    }

    public Resume HandleCreateResume(Resume resume){
        if(resume.getUser()==null || resume.getJob()==null){
            throw new IdInvalidException("User or job must not be left empty.");
        }
        Optional<Job> job = this.jobRepository.findById(resume.getJob().getId());
        Optional<User> user = this.userRepository.findById(resume.getUser().getId());
        resume.setJob(job.orElse(null));
        resume.setUser(user.orElse(null));
        return this.resumeRepository.save(resume);
    }

    public ResResumeDTO ConvertToResResumeDTO(Resume resume){
        ResResumeDTO res = new ResResumeDTO();
        res.setId(resume.getId());
        res.setStatus(resume.getStatus());
        res.setEmail(resume.getEmail());
        res.setUrl(resume.getUrl());
        res.setCreatedBy(resume.getCreatedBy());
        res.setUpdatedAt(resume.getUpdatedAt());
        res.setCreatedAt(resume.getCreatedAt());
        res.setUpdatedBy(resume.getUpdatedBy());
        if(resume.getUser()!=null){
            ResResumeDTO.User user = new ResResumeDTO.User(resume.getUser().getId(), resume.getUser().getName());
            res.setUser(user);
        }
        if(resume.getJob()!= null){
            ResResumeDTO.Job job = new ResResumeDTO.Job(resume.getJob().getId(),resume.getJob().getName());
            res.setJob(job);
        }
        return res;
    }

}
