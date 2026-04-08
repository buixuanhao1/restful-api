package vn.bxh.jobhunter.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.util.Constant.AuthProviderEnum;
import vn.bxh.jobhunter.util.Constant.GenderEnum;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${hao.google.client-id}")
    private String googleClientId;

    private final UserService userService;

    public GoogleAuthService(UserService userService) {
        this.userService = userService;
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
            user.setGender(GenderEnum.OTHER); // Default gender
            user.setAddress("N/A"); // Default address
            user.setPassword("GOOGLE_USER_NO_PASSWORD"); // Dummy password
            this.userService.HandleSaveUser(user);
        } else {
            // Update provider if not already set
            if (user.getAuthProvider() == null) {
                user.setAuthProvider(AuthProviderEnum.GOOGLE);
                // We don't necessarily want to save here if we don't need to change anything else
                // But it's good to keep track of the provider
            }
        }
        return user;
    }
}
