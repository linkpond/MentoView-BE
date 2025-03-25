package ce2team1.mentoview.entity;


import ce2team1.mentoview.entity.atrribute.AuditingFields;
import ce2team1.mentoview.entity.atrribute.Difficulty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class InterviewQuestion extends AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @Column(nullable = false)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @OneToOne(mappedBy = "question", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private InterviewResponse interviewResponse;

    @OneToOne(mappedBy = "question", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private InterviewFeedback interviewFeedback;

    public static InterviewQuestion of(Long questionId) {
        return new InterviewQuestion(questionId, null, null, null, null, null);
    }

    public static InterviewQuestion of(String question, Difficulty difficulty, Interview interview) {
        return new InterviewQuestion (null, question, difficulty, interview, null, null);

    }
    public static InterviewQuestion of(Long questionId, String question, Difficulty difficulty, Interview interview) {
        return new InterviewQuestion (questionId, question, difficulty, interview, null, null);

    }





    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        InterviewQuestion that = (InterviewQuestion) o;
        return getQuestionId() != null && Objects.equals(getQuestionId(), that.getQuestionId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

}
