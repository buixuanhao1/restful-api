package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ReqForgotPasswordDTO {
    @Email(message = "Email khong hop le")
    @NotBlank(message = "Email khong duoc de trong")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
