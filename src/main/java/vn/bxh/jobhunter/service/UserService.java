package vn.bxh.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.Role;
import vn.bxh.jobhunter.domain.request.ReqUserUpdate;
import vn.bxh.jobhunter.domain.response.ResCompanyDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO.Meta;
import vn.bxh.jobhunter.domain.response.ResCreateUserDTO;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.response.ResUserDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.CompanyRepository;
import vn.bxh.jobhunter.repository.RoleRepository;
import vn.bxh.jobhunter.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository,CompanyRepository companyRepository,RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
    }

        public ResCreateUserDTO HandleSaveUser(User user) {
            if(user.getCompany()!=null){
                Optional<Company> companyOptional = this.companyRepository.findById(user.getCompany().getId());
                if(companyOptional.isPresent()){
                    Company company = companyOptional.get();
                    user.setCompany(company);
                }
            }
            if(user.getRole() != null && user.getRole().getId() != 0){
                Optional<Role> roleOptional = this.roleRepository.findById(user.getRole().getId());
                roleOptional.ifPresent(user::setRole);
            } else {
                // Mặc định gán role USER (id=2) nếu không có role
                this.roleRepository.findById(2L).ifPresent(user::setRole);
            }
            return this.convertToResCreateUserDTO(this.userRepository.save(user));
        }

    public void HandleDeleteUser(Long id) {
        this.userRepository.deleteById(id);
    }

    public Optional<User> HandleFetchUserById(Long id) {
        return this.userRepository.findById(id);
    }


    public ResultPaginationDTO HandleFindAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> usePage = this.userRepository.findAll(spec,pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        Meta meta = new Meta(usePage.getNumber()+1, usePage.getSize(), usePage.getTotalPages(), usePage.getTotalElements());
        resultPaginationDTO.setMeta(meta);

        List<ResUserDTO> resUserDTOList = usePage.getContent().stream().map(item ->this.convertToResUserDTO(item)).toList();
        resultPaginationDTO.setResult(resUserDTOList);

        return resultPaginationDTO;
    }

    public ResCompanyDTO ConvertCompanyToResCompanyDTO(Company company){
        ResCompanyDTO res = new ResCompanyDTO();
        res.setId(company.getId());
        res.setName(company.getName());
        res.setAddress(company.getAddress());
        res.setDescription(company.getDescription());
        return res;
    }

    public User HandleUpdateUser(ReqUserUpdate user) {
        Optional<User> userOptional = this.userRepository.findById(user.getId());
        if (userOptional.isPresent()) {
            User newUpdate = userOptional.get();
            newUpdate.setName(user.getName());
            newUpdate.setAge(user.getAge());
            newUpdate.setAddress(user.getAddress());
            newUpdate.setGender(user.getGender());
            return this.userRepository.save(newUpdate);
        }else{
            return null;
        }

    }

    public User HandleSetFreshToken(String email, String refresh_token){
        User user = this.FindUserByEmail(email);
        if(user!=null){
            user.setRefreshToken(refresh_token);
            return this.userRepository.save(user);
        }
        return null;
    }

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO res = new ResCreateUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        if(user.getCompany()!=null){
            res.setCompany(ConvertCompanyToResCompanyDTO(user.getCompany()));
        }

        return res;
    }
    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setUpdatedAt(user.getUpdatedAt());
        if(user.getCompany()!=null){
            res.setCompany(ConvertCompanyToResCompanyDTO(user.getCompany()));
        }
        if(user.getRole()!=null){
            res.setRole(new ResUserDTO.UserRole(user.getRole().getId(),user.getRole().getName()));
        }
        return res;
    }



    public User FindUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public boolean existEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public User FindByEmailAndRefreshToken(String email,String token){
        return this.userRepository.findByEmailAndRefreshToken(email, token);
    }
}
