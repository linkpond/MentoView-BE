package ce2team1.mentoview.repository;

import ce2team1.mentoview.entity.InterviewQuestion;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    @Query("SELECT iq FROM InterviewQuestion iq " +
            "JOIN FETCH iq.interviewResponse ir " +
            "JOIN FETCH iq.interviewFeedback ifb " +
            "WHERE iq.interview.interviewId = :interviewId " +
            "ORDER BY iq.questionId ASC")
    List<InterviewQuestion> findQuestionsWithResponsesAndFeedback(Long interviewId);

    List<InterviewQuestion> findAllByQuestionIdIn(Collection<Long> questionIds);

    @Query("select iq from InterviewQuestion iq " +
            "left join fetch iq.interviewResponse ir " +
            "left join fetch iq.interviewFeedback ifb " +
            "where iq.interview.interviewId in :interviewIds " +
            "order by  iq.questionId ASC")
    @BatchSize(size = 50)
    List<InterviewQuestion> findAllWithResponseAndFeedback(@Param("interviewIds") Collection<Long> interviewIds);
}
