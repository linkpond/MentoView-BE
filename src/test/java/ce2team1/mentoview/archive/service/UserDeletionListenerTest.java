package ce2team1.mentoview.archive.service;

import ce2team1.mentoview.archive.entity.InterviewArchive;
import ce2team1.mentoview.archive.repository.InterviewArchiveRepository;
import ce2team1.mentoview.archive.repository.PaymentArchiveRepository;
import ce2team1.mentoview.archive.repository.UserArchiveRepository;
import ce2team1.mentoview.entity.Interview;
import ce2team1.mentoview.entity.Payment;
import ce2team1.mentoview.entity.Resume;
import ce2team1.mentoview.entity.Subscription;
import ce2team1.mentoview.repository.*;
import ce2team1.mentoview.service.AwsS3Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserDeletionListenerTest {

    @Autowired
    private InterviewRepository interviewRepository;
    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private PaymentArchiveRepository paymentArchiveRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private AwsS3Service awsS3Service;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserArchiveRepository userArchiveRepository ;
    @Autowired
    private InterviewArchiveRepository archiveRepository;
    @Autowired
    private ArchiveService archiveService;
    @Autowired
    private UserDeletionListener  userDeletionListener;

    @Autowired
    private ApplicationEventPublisher eventPublisher; // 이벤트를 트리거하는 역할






    @Test
    public void archive() {
        Long userId = 13L;
        // 이벤트 객체
        UserDeletedEvent event = new UserDeletedEvent(userId);

        // 이벤트 발행
        eventPublisher.publishEvent(event);

        //Listener 호출
        userDeletionListener.deleteUserAfterArchiving(event);

    }

    @Test
    @DisplayName("🔍 UserDeletionListener 동작 테스트 - 유저 삭제")
    public void testDeleteUserAfterArchiving() {
        //  유저 ID
        Long userId = 29L;

        // 삭제 전
        long resumeCountBefore = resumeRepository.count();
        long interviewCountBefore = interviewRepository.count();
        long paymentCountBefore = paymentRepository.count();
        long subscriptionCountBefore = subscriptionRepository.count();
        long userCountBefore = userRepository.count();

        System.out.println(" 삭제 전 데이터 개수");
        System.out.println("Resumes: " + resumeCountBefore);
        System.out.println("Interviews: " + interviewCountBefore);
        System.out.println("Payments: " + paymentCountBefore);
        System.out.println("Subscriptions: " + subscriptionCountBefore);
        System.out.println("Users: " + userCountBefore);

        //  삭제 이벤트 발행
        eventPublisher.publishEvent(new UserDeletedEvent(userId));

        // (이벤트 리스너 호출
        userDeletionListener.deleteUserAfterArchiving(new UserDeletedEvent(userId));

        // 삭제 후 개수
        long resumeCountAfter = resumeRepository.count();
        long interviewCountAfter = interviewRepository.count();
        long paymentCountAfter = paymentRepository.count();
        long subscriptionCountAfter = subscriptionRepository.count();
        long userCountAfter = userRepository.count();

        System.out.println(" 삭제 후 데이터 개수");
        System.out.println("Resumes: " + resumeCountAfter);
        System.out.println("Interviews: " + interviewCountAfter);
        System.out.println("Payments: " + paymentCountAfter);
        System.out.println("Subscriptions: " + subscriptionCountAfter);
        System.out.println("Users: " + userCountAfter);

        //검증
        Assertions.assertEquals(resumeCountBefore - 1, resumeCountAfter, "Resume 삭제 실패");
        Assertions.assertEquals(interviewCountBefore - 1, interviewCountAfter, "Interview 삭제 실패");
        Assertions.assertEquals(paymentCountBefore - 1, paymentCountAfter, "Payment 삭제 실패");
        Assertions.assertEquals(subscriptionCountBefore - 1, subscriptionCountAfter, "Subscription 삭제 실패");
        Assertions.assertEquals(userCountBefore - 1, userCountAfter, "User 삭제 실패");
    }

}