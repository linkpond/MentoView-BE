package ce2team1.mentoview.entity;


import ce2team1.mentoview.entity.atrribute.AuditingFields;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class InterviewFeedback extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String feedback;

    private Integer score;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    private InterviewQuestion question;


    public static InterviewFeedback of(String feedback, Integer score ,  InterviewQuestion question) {
        return new InterviewFeedback (null, feedback, score, question);

    }

    public static InterviewFeedback of(String feedback, Integer score, Long questionId) {
        return new InterviewFeedback (null, feedback, score, InterviewQuestion.of(questionId));
    }

    public static InterviewFeedback of(Long feedbackId, String feedback, Integer score ,  InterviewQuestion question) {
        return new InterviewFeedback (feedbackId, feedback, score, question);

    }


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        InterviewFeedback that = (InterviewFeedback) o;
        return getFeedbackId() != null && Objects.equals(getFeedbackId(), that.getFeedbackId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

