package vn.bxh.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.response.ResCompanyDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO.Meta;
import vn.bxh.jobhunter.domain.response.ResCreateUserDTO;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.response.ResUserDTO;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.CompanyRepository;
import vn.bxh.jobhunter.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public UserService(UserRepository userRepository,CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    public ResCreateUserDTO HandleSaveUser(User user) {
        Optional<Company> companyOptional = this.companyRepository.findById(user.getCompany().getId());
        if(companyOptional.isPresent()){
            Company company = companyOptional.get();
            user.setCompany(company);
        }else {
            user.setCompany(null);
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

        List<ResUserDTO> resUserDTOList = usePage.getContent().stream().map(item -> new ResUserDTO(
                item.getId(),
                item.getEmail(),
                item.getName(),
                item.getGender(),
                item.getAddress(),
                item.getAge(),
                item.getUpdatedAt(),
                item.getCreatedAt(),
                this.ConvertCompanyToResCompanyDTO(item.getCompany()))).toList();
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

    public User HandleUpdateUser(User user) {
        Optional<User> userOptional = this.userRepository.findById(user.getId());
        if (userOptional.isPresent()) {
            User newUser = userOptional.get();
            newUser.setName(user.getName());
            newUser.setEmail(user.getEmail());
            newUser.setAge(user.getAge());
            newUser.setGender(user.getGender());
            Optional<Company> companyOptional = this.companyRepository.findById(user.getCompany().getId());
            if(companyOptional.isPresent()){
                newUser.setCompany(companyOptional.get());
            }
            return newUser;
        }
        return null;
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
            ResCompanyDTO resCompany = new ResCompanyDTO();
            resCompany.setId(user.getCompany().getId());
            resCompany.setName(user.getCompany().getName());
            resCompany.setAddress(user.getCompany().getAddress());
            resCompany.setDescription(user.getCompany().getDescription());
            res.setCompany(resCompany);
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
            ResCompanyDTO resCompany = new ResCompanyDTO();
            resCompany.setId(user.getCompany().getId());
            resCompany.setName(user.getCompany().getName());
            resCompany.setAddress(user.getCompany().getAddress());
            resCompany.setDescription(user.getCompany().getDescription());
            res.setCompany(resCompany);
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
