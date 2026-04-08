package vn.bxh.jobhunter.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReqVerifyPinDTO {
    @Email(message = "Email khong hop le")
    @NotBlank(message = "Email khong duoc de trong")
    private String email;

    @NotBlank(message = "Ma PIN khong duoc de trong")
    @Size(min = 6, max = 6, message = "Ma PIN phai la 6 so")
    private String pin;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
