package ce2team1.mentoview.service;

import ce2team1.mentoview.service.dto.FeedbackDto;
import ce2team1.mentoview.service.dto.GenerateFeedbackDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AiService {

    private final OpenAiChatModel openAiChatModel;

    public Map<Integer, String> getInterviewQuestionFromResume(String extractedText) {
        Map<Integer, String> result = new HashMap<>();
        Message systemMessage =
                new SystemMessage("너는 이제부터 한국어로 답변해주는 면접관이 되는거야. " +
                        "내가 보내주는 이력서 또는 포트폴리오의 텍스트 내용을 보고 면접관으로서 적절한 질문 5가지를 뽑아서 답변해주면 돼. " +
                        "기술적인 질문 위주로 구성되면 좋겠어. " +
                        "질문은 1번부터 5번까지 번호를 매기고, 각 질문은 한 줄로 작성해. 질문 사이에는 줄바꿈이나 공백 없이 'next-question' 만 넣어 구분해줘. " +
                        "다른 설명이나 문장은 추가하지 말고 질문만 제공해.");

        Message userMessage = new UserMessage(extractedText);
        ChatResponse response = openAiChatModel.call(new Prompt(systemMessage, userMessage));

        String[] questions = response.getResult().getOutput().getText().split("next-question");

        for (int i = 0; i < questions.length; i++) {
            result.put(i + 1, questions[i].replaceAll("\n", "").trim());
        }

        return result;
    }

    @Async(value = "generateFeedbackExecutor")
    public CompletableFuture<FeedbackDto> generateFeedbackFromQA(GenerateFeedbackDto request) {
        Message systemMessage =
                new SystemMessage("너는 이제부터 한국어로 답변해주는 면접관이 되는거야. " +
                        "너가 제공한 면접 질문은 아래와 같아. \n" +
                        request.getQuestion() +
                        "\n해당 면접 질문과 응답을 보고 피드백을 생성해줘. " +
                        "마크다운은 사용하지 말고 오직 피드백만 제공해. " +
                        "\n만약 답변이 너무 짧거나 없으면 피드백으로 모범답안을 제공해.");

        UserMessage userMessage = new UserMessage(request.getAnswer());
        ChatResponse response = openAiChatModel.call(new Prompt(systemMessage, userMessage));

        String feedback = response.getResult().getOutput().getText().replaceAll("\n", "").trim();

        return CompletableFuture.completedFuture(FeedbackDto.of(feedback, 10, request.getQuestionId()));
    }
}
