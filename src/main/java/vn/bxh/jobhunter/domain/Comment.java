package vn.bxh.jobhunter.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "comments")
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @JsonIgnoreProperties({"comments", "author", "content"})
    private Post post;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonIgnoreProperties({"resumes", "password", "refreshToken", "role", "company"})
    private User author;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
    }
}
