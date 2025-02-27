package vn.bxh.jobhunter.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.service.UserService;
import vn.bxh.jobhunter.service.error.IdInvalidException;

@RestController
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createNewUser(@RequestBody User user) {
        String passwordEncode = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordEncode);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.HandleSaveUser(user));
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateNewUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.HandleUpdateUser(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) throws IdInvalidException {
        if (id > 100) {
            throw new IdInvalidException("id không được lớn hơn 100");
        }
        this.userService.HandleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body("hao-huengmin");
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> FetchUserById(@PathVariable Long id) {
        return ResponseEntity.ok(this.userService.HandleFetchUserById(id).get());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> FetchAllUser() {
        return ResponseEntity.ok(this.userService.HandleFindAllUsers());
    }

}
