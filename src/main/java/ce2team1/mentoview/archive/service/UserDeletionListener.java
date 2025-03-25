package ce2team1.mentoview.archive.service;

import ce2team1.mentoview.entity.Interview;
import ce2team1.mentoview.entity.Payment;
import ce2team1.mentoview.entity.Resume;
import ce2team1.mentoview.entity.Subscription;
import ce2team1.mentoview.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDeletionListener {
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final ResumeRepository resumeRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteUserAfterArchiving(UserDeletedEvent event) {
        Long userId = event.getUserId();
        log.info("[유저 데이터 삭제 진행] UserId {} ", userId);

        List<Long> resumeIds = resumeRepository.findAllByUserId(userId)
                .stream().map(Resume::getResumeId).toList();

        List<Long> interviewIds = interviewRepository.findAllByResumeId(resumeIds)
                .stream().map(Interview::getInterviewId).toList();

        List<Long> subIds = subscriptionRepository.findAllByUser_UserId(userId)
                .stream().map(Subscription::getSubId).toList();

        List<Long> paymentIds = paymentRepository.findBySubIds(subIds)
                .stream().map(Payment::getPaymentId).toList();

        log.info("[삭제 대상 조회 완료] Resume: {}, Interview: {}, Subscription: {}, Payment: {}",
                resumeIds.size(), interviewIds.size(), subIds.size(), paymentIds.size());

        if (!interviewIds.isEmpty()) {
            deleteInterviewResponsesInBatch(interviewIds);
            log.info("[삭제 완료] InterviewResponses 삭제 완료");
        }


        if (!interviewIds.isEmpty()) {
            deleteInterviewFeedbacksInBatch(interviewIds);
            log.info("[삭제 완료] InterviewFeedbacks 삭제 완료");
        }

        if (!interviewIds.isEmpty()) {
            deleteInterviewQuestionsInBatch(interviewIds);
            log.info("[삭제 완료] InterviewQuestions 삭제 완료");
        }

        if (!paymentIds.isEmpty()) {
            deletePaymentsInBatch(paymentIds);
            log.info("[삭제 완료] Payments 삭제 완료");
        }

        if (!subIds.isEmpty()) {
            deleteSubscriptionsInBatch(subIds);
            log.info("[삭제 완료] Subscriptions 삭제 완료");
        }

        if (!interviewIds.isEmpty()) {
            deleteInterviewsInBatch(interviewIds);
            log.info("[삭제 완료] Interviews 삭제 완료");
        }

        if (!resumeIds.isEmpty()) {
            deleteResumesInBatch(resumeIds);
            log.info("[삭제 완료] Resumes 삭제 완료");
        }

        // ✅ 6. 최종적으로 유저 삭제
        userRepository.deleteById(userId);
        log.info("[유저 삭제 완료] UserId: {}", userId);
    }
    private void deleteInterviewResponsesInBatch(List<Long> interviewIds) {
        Query query = entityManager.createQuery(
                "DELETE FROM InterviewResponse ir WHERE ir.question.interview.interviewId IN :interviewIds"
        );
        query.setParameter("interviewIds", interviewIds);
        query.executeUpdate();
        entityManager.clear();
    }

    private void deleteInterviewFeedbacksInBatch(List<Long> interviewIds) {
        Query query = entityManager.createQuery(
                "DELETE FROM InterviewFeedback ifb WHERE ifb.question.interview.interviewId IN :interviewIds"
        );
        query.setParameter("interviewIds", interviewIds);
        query.executeUpdate();
        entityManager.clear();
    }


    private void deleteInterviewQuestionsInBatch(List<Long> interviewIds) {
        Query query = entityManager.createQuery(
                "DELETE FROM InterviewQuestion iq WHERE iq.interview.interviewId IN :interviewIds"
        );
        query.setParameter("interviewIds", interviewIds);
        query.executeUpdate();
        entityManager.clear();
    }

    private void deletePaymentsInBatch(List<Long> paymentIds) {
        Query query = entityManager.createQuery("delete from Payment p where p.id in :paymentIds");
        query.setParameter("paymentIds", paymentIds);
        query.executeUpdate();
        entityManager.clear();
    }

    private void deleteSubscriptionsInBatch(List<Long> subIds) {
        Query query = entityManager.createQuery("delete from Subscription s where s.id in :subIds");
        query.setParameter("subIds", subIds);
        query.executeUpdate();
        entityManager.clear();
    }

    private void deleteInterviewsInBatch(List<Long> interviewIds) {
        Query query = entityManager.createQuery("delete from Interview i where i.id in :interviewIds");
        query.setParameter("interviewIds", interviewIds);
        query.executeUpdate();
        entityManager.clear();
    }

    private void deleteResumesInBatch(List<Long> resumeIds) {
        Query query = entityManager.createQuery("delete from Resume r where r.id in :resumeIds");
        query.setParameter("resumeIds", resumeIds);
        query.executeUpdate();
        entityManager.clear();
    }


}
