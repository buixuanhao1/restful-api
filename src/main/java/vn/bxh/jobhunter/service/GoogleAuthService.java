package vn.bxh.jobhunter.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.Role;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.repository.RoleRepository;
import vn.bxh.jobhunter.util.Constant.AuthProviderEnum;
import vn.bxh.jobhunter.util.Constant.GenderEnum;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${hao.google.client-id}")
    private String googleClientId;

    private final UserService userService;
    private final RoleRepository roleRepository;

    public GoogleAuthService(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    public GoogleIdToken.Payload verifyToken(String tokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(tokenString);
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new IllegalArgumentException("Invalid Google ID Token");
        }
    }

    public User processGoogleUser(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        User user = this.userService.FindUserByEmail(email);

        if (user == null) {
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setName((String) payload.get("name"));
            user.setAuthProvider(AuthProviderEnum.GOOGLE);
            user.setAge(18); // Default age
            user.setGender(GenderEnum.MALE); // Use MALE as safer default for DB constraints
            user.setAddress("N/A"); // Default address
            user.setPassword("GOOGLE_USER_NO_PASSWORD"); // Dummy password
            
            // Assign default role (ID 2 or various 'USER' names)
            Role role = this.roleRepository.findByName("USER")
                    .orElseGet(() -> this.roleRepository.findByName("User")
                        .orElseGet(() -> this.roleRepository.findByName("user")
                            .orElseGet(() -> this.roleRepository.findById(2L).orElse(null))));
            
            if (role == null) {
                throw new IdInvalidException("Default role (USER or ID 2) not found in database. Cannot create Google user.");
            }
            user.setRole(role);

            this.userService.HandleSaveUser(user);
        } else {
            // Update provider if not already set
            if (user.getAuthProvider() == null) {
                user.setAuthProvider(AuthProviderEnum.GOOGLE);
                // Save the updated user
                this.userService.HandleSaveUser(user);
            }
        }
        return user;
    }
}
