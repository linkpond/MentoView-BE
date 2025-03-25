package ce2team1.mentoview.entity;


import ce2team1.mentoview.entity.atrribute.AuditingFields;
import ce2team1.mentoview.entity.atrribute.InterviewStatus;
import ce2team1.mentoview.entity.atrribute.InterviewType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Interview extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Long interviewId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus interviewStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType interviewType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<InterviewQuestion> questionList = new ArrayList<>();


    public static Interview of(InterviewStatus interviewStatus, InterviewType interviewType, Resume resume) {
        return new Interview (null, interviewStatus, interviewType, resume, new ArrayList<>());

    }
    public static Interview of(Long interviewId, InterviewStatus interviewStatus, InterviewType interviewType, Resume resume) {
        return new Interview (interviewId, interviewStatus, interviewType, resume, new ArrayList<>());

    }

    public void updateStatus(InterviewStatus status) {
        this.interviewStatus = status;
    }


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Interview interview = (Interview) o;
        return getInterviewId() != null && Objects.equals(getInterviewId(), interview.getInterviewId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
