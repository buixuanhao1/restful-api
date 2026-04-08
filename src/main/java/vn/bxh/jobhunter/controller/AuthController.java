package vn.bxh.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.request.ReqLoginDTO;
import vn.bxh.jobhunter.domain.request.ReqRegisterDTO;
import vn.bxh.jobhunter.domain.response.ResCreateUserDTO;
import vn.bxh.jobhunter.domain.response.ResLoginDTO;
import vn.bxh.jobhunter.domain.request.ReqForgotPasswordDTO;
import vn.bxh.jobhunter.domain.request.ReqVerifyPinDTO;
import vn.bxh.jobhunter.domain.request.ReqResetPasswordDTO;
import vn.bxh.jobhunter.service.UserService;
import vn.bxh.jobhunter.service.EmailService;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.anotation.ApiMessage;
import vn.bxh.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${hao.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
                          UserService userService, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * POST /api/v1/auth/register
     * Registers a new user (Candidate role by default).
     * Uses a strict DTO to prevent over-posting (users cannot set their own role/id).
     */
    @PostMapping("/auth/register")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody ReqRegisterDTO dto) {
        if (this.userService.existEmail(dto.getEmail())) {
            throw new IdInvalidException("Email '" + dto.getEmail() + "' da duoc su dung");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.registerUser(dto, passwordEncoder));
    }

    /**
     * POST /api/v1/auth/login
     * Authenticates an existing user and returns JWT tokens.
     */
    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLoginDTO) {
        User userDB = this.userService.FindUserByEmail(reqLoginDTO.getUsername());
        if (userDB == null) {
            throw new IdInvalidException("Email hoac mat khau khong dung!");
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(reqLoginDTO.getUsername(), reqLoginDTO.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(userDB.getId());
        userLogin.setName(userDB.getName());
        userLogin.setEmail(userDB.getEmail());
        userLogin.setRole(userDB.getRole());
        userLogin.setCompany(userDB.getCompany());

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setUser(userLogin);
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), resLoginDTO);
        resLoginDTO.setAccessToken(accessToken);

        String refreshToken = this.securityUtil.createRefreshToken(reqLoginDTO.getUsername(), resLoginDTO);
        this.userService.HandleSetFreshToken(reqLoginDTO.getUsername(), refreshToken);

        ResponseCookie springCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                .body(resLoginDTO);
    }

    /**
     * GET /api/v1/auth/account
     * Returns the currently authenticated user's profile.
     */
    @GetMapping("/auth/account")
    @ApiMessage("Fetch Account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User userDB = this.userService.FindUserByEmail(email);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();

        if (userDB != null) {
            userLogin.setId(userDB.getId());
            userLogin.setName(userDB.getName());
            userLogin.setEmail(userDB.getEmail());
            userLogin.setRole(userDB.getRole());
            userLogin.setCompany(userDB.getCompany());
        }
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        userGetAccount.setUser(userLogin);
        return ResponseEntity.ok(userGetAccount);
    }

    /**
     * GET /api/v1/auth/refresh
     * Issues a new access token using a valid refresh token from cookie.
     */
    @GetMapping("/auth/refresh")
    @ApiMessage("Get user by refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(@CookieValue(name = "refresh_token") String refreshToken) {
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        User userDB = this.userService.FindByEmailAndRefreshToken(email, refreshToken);
        if (userDB == null) {
            throw new IdInvalidException("Refresh token khong hop le hoac da het han");
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(userDB.getId());
        userLogin.setName(userDB.getName());
        userLogin.setEmail(userDB.getEmail());
        userLogin.setRole(userDB.getRole());

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setUser(userLogin);
        String newAccessToken = this.securityUtil.createAccessToken(email, resLoginDTO);
        resLoginDTO.setAccessToken(newAccessToken);

        String newRefreshToken = this.securityUtil.createRefreshToken(email, resLoginDTO);
        this.userService.HandleSetFreshToken(email, newRefreshToken);

        ResponseCookie springCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                .body(resLoginDTO);
    }

    /**
     * POST /api/v1/auth/logout
     * Clears the refresh token and expires the cookie.
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<Object> logoutOut() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        this.userService.HandleSetFreshToken(email, null);

        ResponseCookie deleteSpringCookie = ResponseCookie.from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .build();
    }
    @PostMapping("/auth/forgot-password")
    @ApiMessage("Gui ma OTP thanh cong")
    public ResponseEntity<Object> forgotPassword(@Valid @RequestBody ReqForgotPasswordDTO dto) {
        User userDB = this.userService.FindUserByEmail(dto.getEmail());
        if (userDB == null) {
            throw new IdInvalidException("Email khong ton tai trong he thong");
        }
        String otp = this.userService.generateAndSaveOtp(dto.getEmail());
        this.emailService.sendOtpEmail(dto.getEmail(), userDB.getName(), otp);
        return ResponseEntity.ok(null);
    }

    /**
     * POST /api/v1/auth/verify-otp
     * Step 2: Verify OTP
     */
    @PostMapping("/auth/verify-otp")
    @ApiMessage("Xac thuc OTP thanh cong")
    public ResponseEntity<Object> verifyOtp(@Valid @RequestBody ReqVerifyPinDTO dto) {
        this.userService.verifyOtp(dto.getEmail(), dto.getPin());
        return ResponseEntity.ok(null);
    }

    /**
     * POST /api/v1/auth/reset-password
     * Step 3: Reset password
     */
    @PostMapping("/auth/reset-password")
    @ApiMessage("Dat lai mat khau thanh cong")
    public ResponseEntity<Object> resetPassword(@Valid @RequestBody ReqResetPasswordDTO dto) {
        this.userService.resetPassword(dto.getEmail(), dto.getPin(), dto.getNewPassword(), this.passwordEncoder);
        return ResponseEntity.ok(null);
    }
}
