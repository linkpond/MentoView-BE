package ce2team1.mentoview.archive.service;

import ce2team1.mentoview.archive.dto.InterviewData;
import ce2team1.mentoview.archive.dto.InterviewEntry;
import ce2team1.mentoview.archive.entity.*;
import ce2team1.mentoview.archive.repository.InterviewArchiveRepository;
import ce2team1.mentoview.archive.repository.PaymentArchiveRepository;
import ce2team1.mentoview.archive.repository.UserArchiveRepository;
import ce2team1.mentoview.entity.*;
import ce2team1.mentoview.entity.atrribute.UserStatus;
import ce2team1.mentoview.repository.*;
import ce2team1.mentoview.service.AwsS3Service;
import ce2team1.mentoview.utils.archive.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveService {
    private final InterviewArchiveRepository archiveRepository;
    private final ResumeRepository resumeRepository;
    private final PaymentArchiveRepository paymentArchiveRepository;
    private final InterviewRepository interviewRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AwsS3Service awsS3Service;
    private final UserRepository userRepository;
    private final UserArchiveRepository userArchiveRepository ;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final ApplicationEventPublisher eventPublisher;
/*
* todo
*  lamdba 작업이 필요한듯
* */
    // Interview Archive 생성
    @Transactional(readOnly = false)
    public InterviewArchive archiveUserInterviews(Long userId) {
        List<Long> resumesId = resumeRepository.findAllByUserId(userId).stream().map(Resume::getResumeId).collect(Collectors.toList());
        if (resumesId.isEmpty()) {
            return InterviewArchive.of(0L,"");
        }
        System.out.println("resumesId = " + resumesId);

        List<Interview> interviewList = interviewRepository.findAllByResumeId(resumesId);
        if (interviewList.isEmpty()) {
            return InterviewArchive.of(0L,"");
        }

        List<Long> interviewIds = interviewList.stream().map(Interview::getInterviewId).collect(Collectors.toList());

        //interviewId 기준 그룹핑
        Map<Long, List<InterviewEntry>> entryGroup = interviewQuestionRepository.findAllWithResponseAndFeedback(interviewIds)
                .stream().map(InterviewEntry::of)
                .collect(Collectors.groupingBy(InterviewEntry::getInterviewId));


        List<InterviewData> allInterviewData = interviewList.stream().map(interview
                        -> InterviewData.of(entryGroup.getOrDefault(interview.getInterviewId(), new ArrayList<>())))
                .collect(Collectors.toList());


        String interviewDataJson = JsonUtils.toJson(allInterviewData);
        System.out.println("interviewDataJson = " + interviewDataJson);

        String s3key = awsS3Service.interviewArchiveUpload(userId, interviewDataJson);

        InterviewArchive save = archiveRepository.save(InterviewArchive.of(userId, s3key));
        return save;


    }

    @Transactional(readOnly = false)
    public List<PaymentArchive> archiveUserPayments(Long userId) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByUser_UserId(userId);
        if (subscriptions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> subIds = subscriptions.stream().map(subscription -> subscription.getSubId())
                .collect(Collectors.toList());

        List<Payment> paymentList = paymentRepository.findBySubIds(subIds);

        List<PaymentArchive> paymentArchiveList = paymentList.stream().map(
                        payment -> PaymentArchive.of(
                                userId,
                                payment.getAmount(),
                                payment.getApprovalCode(),
                                payment.getSubscription().getPlan().name(),
                                payment.getStatus(),
                                payment.getPaymentDate()
                        ))
                .collect(Collectors.toList());


        List<PaymentArchive> paymentArchives = paymentArchiveRepository.saveAll(paymentArchiveList);
        return paymentArchives;

    }

    @Transactional(readOnly = false)
    public void archiveUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));

        UserArchive userArchive = UserArchive.of(
                userId,
                user.getEmail(),
                user.getRole(),
                UserStatus.DELETED,
                user.getCreatedAt(),
                ""
        );
        userArchiveRepository.save(userArchive);
        log.info("archive userId = " + userId);

        eventPublisher.publishEvent(new UserDeletedEvent(userId));

    }



}
