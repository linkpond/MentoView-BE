package ce2team1.mentoview.archive.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Builder
@NoArgsConstructor
@Getter
@AllArgsConstructor
@Entity
@ToString
public class InterviewArchive  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 255, nullable = false)
    private String interviewKey; // S3 키 값

    @Column(nullable = false)
    private LocalDateTime archivedAt; // 아카이브된 시간


    public static InterviewArchive of(Long userId, String interviewKey) {
        return InterviewArchive.builder()
                .userId(userId)
                .interviewKey(interviewKey)
                .archivedAt(LocalDateTime.now())
                .build();
    }
}



