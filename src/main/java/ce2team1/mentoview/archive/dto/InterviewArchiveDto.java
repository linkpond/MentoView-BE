package ce2team1.mentoview.archive.dto;

import ce2team1.mentoview.archive.entity.InterviewArchive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO for {@link ce2team1.mentoview.archive.entity.InterviewArchive}
 */
@AllArgsConstructor
@Getter
@Builder
public class InterviewArchiveDto {
    private final Long userId;
    private final String interviewKey;


    public static InterviewArchiveDto of(Long userId, String interviewKey){
        return new InterviewArchiveDto(
                userId,
                interviewKey
        );
    }

    public static InterviewArchiveDto by(InterviewArchive interviewArchive){
        return InterviewArchiveDto.builder()
                .userId(interviewArchive.getUserId())
                .interviewKey(interviewArchive.getInterviewKey())
                .build();

    }
}