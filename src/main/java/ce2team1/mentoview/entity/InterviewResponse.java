package ce2team1.mentoview.entity;


import ce2team1.mentoview.entity.atrribute.AuditingFields;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.Duration;
import java.util.Objects;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class InterviewResponse extends AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    private Long responseId;

    @Column(name = "s3_key")
    private String s3Key;// 음성파일 저장 s3 key

    @Lob
    @Column(columnDefinition = "TEXT")
    private String response; // AWS Transcribe 가 변환

    @Column(nullable = false)
    private Boolean answered; // 상태 필드 3가 필요시 Enum

    @Column(nullable = false)
    private Duration duration; // 응답 제출 안했을 시 0초 지정

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    private InterviewQuestion question;

    public static InterviewResponse of(String s3Key, String response,Boolean answered, Duration duration,InterviewQuestion question) {
        return new InterviewResponse (null, s3Key, response, answered, duration, question);
    }
    public static InterviewResponse of(Long responseId, String s3Key, String response,Boolean answered, Duration duration,InterviewQuestion question) {
        return new InterviewResponse (responseId, s3Key, response,answered, duration,question );
    }

    // 변환 텍스트 업데이트
    public void updateTranscription(String transcriptionText) {
        this.response = transcriptionText;
    }






    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        InterviewResponse that = (InterviewResponse) o;
        return getResponseId() != null && Objects.equals(getResponseId(), that.getResponseId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
