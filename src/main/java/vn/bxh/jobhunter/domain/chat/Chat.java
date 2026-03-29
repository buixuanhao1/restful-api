package vn.bxh.jobhunter.domain.chat;

import java.time.Instant;

import vn.bxh.jobhunter.domain.User;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chats")
@Getter
@Setter
public class Chat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "TEXT")
    private String content;
    private boolean isRead;
    private Instant createdAt;
    
    @ManyToOne
    @JoinColumn(name = "sender_id")
    @JsonIgnoreProperties({"resumes", "password", "refreshToken", "role", "company"})
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    @JsonIgnoreProperties({"resumes", "password", "refreshToken", "role", "company"})
    private User receiver;

    @PrePersist
    public void handleBeforeCreate()
    {
        this.createdAt = Instant.now();
    }


}
