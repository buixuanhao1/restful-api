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
        private RoleLogin role;
        private CompanyLogin company;
        private Integer age;
        private vn.bxh.jobhunter.util.Constant.GenderEnum gender;
        private String address;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleLogin {
        private long id;
        private String name;
        private java.util.List<PermissionLogin> permissions;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PermissionLogin {
        private long id;
        private String name;
        private String apiPath;
        private String method;
        private String module;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompanyLogin {
        private long id;
        private String name;
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
