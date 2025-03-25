package ce2team1.mentoview.archive.dto;

import ce2team1.mentoview.entity.InterviewQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterviewEntry {

    private Long interviewId; // 인터뷰 id
    private int  questionNumber; // 번호

    private String question; // 질문 내용
    private String response; // 응답 내용
    private String feedback; // 피드백 내용

    public static InterviewEntry of(InterviewQuestion question) {
        return InterviewEntry.builder()
                .interviewId(question.getInterview().getInterviewId())
                .questionNumber(question.getQuestionId().intValue())
                .question(question.getQuestion())
                .response(question.getInterviewResponse()!= null? question.getInterviewResponse().getResponse() : "")
                .feedback(question.getInterviewFeedback()!= null? question.getInterviewFeedback().getFeedback() : "")
                .build();
    }
}
