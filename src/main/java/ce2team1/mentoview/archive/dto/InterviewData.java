package ce2team1.mentoview.archive.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterviewData {

    private String interviewId;

    private List<InterviewEntry> interviewEntries;

    public static InterviewData of(List<InterviewEntry> entries) {
        return InterviewData.builder()
                .interviewId(UUID.randomUUID().toString())
                .interviewEntries(entries)
                .build();
    }
}
