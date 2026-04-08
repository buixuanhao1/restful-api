package vn.bxh.jobhunter.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.request.GoogleLoginRequest;
import vn.bxh.jobhunter.domain.response.ResLoginDTO;
import vn.bxh.jobhunter.service.GoogleAuthService;
import vn.bxh.jobhunter.service.UserService;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/v1")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final UserService userService;
    private final SecurityUtil securityUtil;

    @Value("${hao.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public GoogleAuthController(GoogleAuthService googleAuthService, UserService userService, SecurityUtil securityUtil) {
        this.googleAuthService = googleAuthService;
        this.userService = userService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/auth/google-login")
    public ResponseEntity<ResLoginDTO> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        try {
            GoogleIdToken.Payload payload = googleAuthService.verifyToken(request.getToken());
            User user = googleAuthService.processGoogleUser(payload);

            // Create Authentication object manually
            // Since we don't have a password for Google users in the usual sense, 
            // we bypass the normal authenticationManager authentication
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
            userLogin.setId(user.getId());
            userLogin.setName(user.getName());
            userLogin.setEmail(user.getEmail());
            userLogin.setRole(user.getRole());
            userLogin.setCompany(user.getCompany());

            ResLoginDTO resLoginDTO = new ResLoginDTO();
            resLoginDTO.setUser(userLogin);

            String access_token = this.securityUtil.createAccessToken(user.getEmail(), resLoginDTO);
            resLoginDTO.setAccessToken(access_token);
            
            String refresh_token = this.securityUtil.createRefreshToken(user.getEmail(), resLoginDTO);
            this.userService.HandleSetFreshToken(user.getEmail(), refresh_token);

            ResponseCookie springCookie = ResponseCookie.from("refresh_token", refresh_token)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(refreshTokenExpiration)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                    .body(resLoginDTO);

        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            throw new IdInvalidException("Google authentication failed: " + e.getMessage());
        }
    }
}
