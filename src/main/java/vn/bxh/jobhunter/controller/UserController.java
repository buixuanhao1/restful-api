package vn.bxh.jobhunter.controller;

import java.util.Optional;

import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.request.ReqUserUpdate;
import vn.bxh.jobhunter.domain.response.ResCreateUserDTO;
import vn.bxh.jobhunter.domain.response.ResUserDTO;
import vn.bxh.jobhunter.domain.response.RestResponse;
import vn.bxh.jobhunter.domain.response.ResultPaginationDTO;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.service.UserService;
import vn.bxh.jobhunter.util.anotation.ApiMessage;
import vn.bxh.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserController(UserService userService, PasswordEncoder passwordEncoder,UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/users")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User user) {

        boolean existsEmail = this.userService.existEmail(user.getEmail());
        if (existsEmail == true) {
            throw new IdInvalidException("Email not found!" + user.getEmail());
        }
        String passwordEncode = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordEncode);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.HandleSaveUser(user));
    }

    @PutMapping("/users")
    public ResponseEntity<ResUserDTO> updateNewUser(@RequestBody ReqUserUpdate userUpdate) {
        User user1 = this.userService.HandleUpdateUser(userUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToResUserDTO(user1));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Xóa người dùng thành công")
    public void deleteUser(@PathVariable Long id) {
        Optional<User> user = this.userRepository.findById(id);
        if(user.isEmpty()){
            throw new IdInvalidException("Id not exists!");
        }
        this.userService.HandleDeleteUser(id);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ResUserDTO> FetchUserById(@PathVariable Long id) {
        Optional<User> user = this.userRepository.findById(id);
        if(!user.isPresent()){
            throw new IdInvalidException("Id not exists!");
        }
        return ResponseEntity.ok(this.userService.convertToResUserDTO(user.get()));
    }

    @GetMapping("/users")
    @ApiMessage("Fetch all users")
    public ResponseEntity<ResultPaginationDTO> FetchAllUser(@Filter Specification<User> spec, Pageable page) {
        return ResponseEntity.ok(this.userService.HandleFindAllUsers(spec, page));
    }

    @PutMapping("/users/profile")
    @ApiMessage("Update profile thành công")
    public ResponseEntity<ResUserDTO> updateProfile(@RequestBody ReqUserUpdate profileUpdate) {
        String email = vn.bxh.jobhunter.util.SecurityUtil.getCurrentUserLogin().orElse("");
        User currentUser = this.userService.FindUserByEmail(email);
        if (currentUser == null) {
            throw new vn.bxh.jobhunter.util.error.IdInvalidException("User not found!");
        }
        profileUpdate.setId(currentUser.getId()); // Force update current user
        User updated = this.userService.HandleUpdateUser(profileUpdate);
        return ResponseEntity.ok(this.userService.convertToResUserDTO(updated));
    }

}
