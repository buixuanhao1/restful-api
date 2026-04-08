package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReqVerifyPinDTO {
    @Email(message = "Email khong hop le")
    @NotBlank(message = "Email khong duoc de trong")
    private String email;

    @NotBlank(message = "Ma OTP khong duoc de trong")
    @Size(min = 6, max = 6, message = "Ma OTP phai la 6 so")
    private String otp;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
