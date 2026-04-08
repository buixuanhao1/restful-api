package vn.bxh.jobhunter.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import vn.bxh.jobhunter.util.Constant.GenderEnum;
import vn.bxh.jobhunter.util.Constant.AuthProviderEnum;
import vn.bxh.jobhunter.util.SecurityUtil;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Name cannot be empty!")
    private String name;

    @Email(message = "Invalid email format!")
    @NotBlank(message = "Email cannot be empty!")
    private String email;

    @Size(min = 3, message = "Password must be at least 3 characters!")
    private String password;

    @NotNull(message = "Age cannot be null!")
    @Min(value = 16, message = "Age must be at least 16!")
    private Integer age;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    @NotBlank(message = "Address cannot be empty!")
    private String address;

    @Enumerated(EnumType.STRING)
    private AuthProviderEnum authProvider;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String refreshToken;

    // Password reset OTP fields
    private String resetPin;
    private Instant resetPinExpiry;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    @OneToMany( mappedBy = "user")
    List<Resume> resumes;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        this.updatedAt = Instant.now();
    }
}
