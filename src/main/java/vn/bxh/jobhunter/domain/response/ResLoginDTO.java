package vn.bxh.jobhunter.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import vn.bxh.jobhunter.domain.Company;
import vn.bxh.jobhunter.domain.Role;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResLoginDTO {
    @JsonProperty("access_token")
    private String accessToken;
    private UserLogin user;

    @Getter
    @Setter
    public static class UserLogin{
        private long id;
        private String email;
        private String name;
        private Role role;
        private Company company;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInsideToken {
        private long id;
        private String email;
        private String name;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserGetAccount{
        private UserLogin user;
    }
}
