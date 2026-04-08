package vn.bxh.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResSavedJobDTO {
    private long id;
    private Instant createdAt;
    private JobSaved job;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobSaved {
        private long id;
        private String name;
        private String location;
        private double salary;
        private CompanySaved company;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompanySaved {
        private long id;
        private String name;
        private String logo;
    }
}
