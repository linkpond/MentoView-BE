package ce2team1.mentoview.service;


import ce2team1.mentoview.controller.dto.response.SubscriptionResp;
import ce2team1.mentoview.entity.Subscription;
import ce2team1.mentoview.entity.User;
import ce2team1.mentoview.entity.atrribute.PaymentMethod;
import ce2team1.mentoview.entity.atrribute.SubscriptionPlan;
import ce2team1.mentoview.entity.atrribute.SubscriptionStatus;
import ce2team1.mentoview.repository.SubscriptionRepository;
import ce2team1.mentoview.repository.UserRepository;
import ce2team1.mentoview.service.dto.PaymentCheckDto;
import ce2team1.mentoview.service.dto.SubscriptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public List<SubscriptionResp> getSubscriptions(Long uId) {

        return subscriptionRepository.findAllByUser_UserId(uId)
                                    .stream()
                                    .map(subscription -> {
                                        SubscriptionResp resp = SubscriptionResp.toResp(SubscriptionDto.toDto(subscription));
                                        return resp;
                                    })
                                    .collect(Collectors.toList());

    }

    @Transactional
    public void deleteSubscription(Long sId) {

        // 삭제 시 디비에서 삭제하는 게 아니라 status를 변경
        Subscription subscription = subscriptionRepository.findById(sId).orElseThrow();
        subscription.modifyStatus(SubscriptionStatus.CANCELED);
    }

    @Transactional
    public void deleteSubscriptionByPaymentId(String paymentId) {

        // 삭제 시 디비에서 삭제하는 게 아니라 status를 변경
        Subscription subscription = subscriptionRepository.findByPortonePaymentId(paymentId);
        if (subscription != null) {
            subscription.modifyStatus(SubscriptionStatus.EXPIRY);
        }
    }

    public SubscriptionDto createSubscription(PaymentCheckDto paymentCheckDto) {
        // paidAt 포맷팅
        String paidAtString = paymentCheckDto.getPaidAt();
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(paidAtString, DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(ZoneId.of("Asia/Seoul"));;
        LocalDate ld = zonedDateTime.toLocalDate();

        User user = userRepository.findById(Long.valueOf(paymentCheckDto.getCustomer().getId())).orElseThrow();
        System.out.println(user.getUserId());

        PaymentMethod paymentMethod = "KAKAOPAY".equals(paymentCheckDto.getMethod().getProvider())? PaymentMethod.KAKAO_PAY : PaymentMethod.CREDIT_CARD;
        return SubscriptionDto.toDto(subscriptionRepository.save(Subscription.of(
                                                                    SubscriptionStatus.ACTIVE,
                                                                    SubscriptionPlan.BASIC,
                                                                    ld,
                                                                    ld.plusDays(30),
                                                                    ld.plusDays(31),
                                                                    paymentMethod,
                                                                    null,
                                                                    null,
                                                                    user)
                                                                ));
    }

    public void createFreeTierSubscription(Long uId) {

        LocalDate ld = LocalDate.now();
        User user = userRepository.findById(uId).orElseThrow();

        subscriptionRepository.save(Subscription.of(
                SubscriptionStatus.ACTIVE,
                SubscriptionPlan.FREE_TIER,
                ld,
                ld.plusDays(30),
                ld.plusDays(31),
                PaymentMethod.KAKAO_PAY,
                null,
                null,
                user)
        );
    }

    public Long checkSubscription(Long uId) {

        Subscription subscription = subscriptionRepository.findByUser_UserIdAndStatus(uId, SubscriptionStatus.ACTIVE);

        if (subscription != null) {
            return subscription.getSubId();
        }
        return null;
    }

    @Transactional
    public SubscriptionDto modifyEndDateAndNextBillingDate(Long subId, String paidAt) {

        Subscription subscription = subscriptionRepository.findById(subId).orElseThrow();
        subscription.modifyEndDateAndNextBillingDateAndPlan(paidAt);

        return SubscriptionDto.toDto(subscription);
    }

//    public String getBillingKey(Long sId) {
//        Subscription subscription = subscriptionRepository.findById(sId).orElseThrow();
//        return subscription.getBillingKey();
//    }


    @Transactional
    public void initPaymentScheduleIdAndPaymentId(Long uId, String paymentId, String scheduleId) {
        Subscription subscription = subscriptionRepository.findByUser_UserIdAndStatus(uId, SubscriptionStatus.ACTIVE);

        if (subscription == null) {
            subscription = subscriptionRepository.findByUser_UserIdAndStatus(uId, SubscriptionStatus.CANCELED);
        }
        subscription.setPaymentIdAndScheduleId(paymentId, scheduleId);
    }

    public Subscription getSubscriptionByUserId(Long uId, SubscriptionStatus status) {
        Subscription subscription = subscriptionRepository.findByUser_UserIdAndStatus(uId, status);

        return subscription;
    }

//    @Transactional
//    public void modifyBillingKey(Long sId, String billingKey) {
//
//        Subscription subscription = subscriptionRepository.findById(sId).orElseThrow();
//        subscription.modifyBillingKey(billingKey);
//    }

    public List<Subscription> findCanceledSubscriptionsOfToday(LocalDate today) {

        return subscriptionRepository.findByStatusAndNextBillingDate(SubscriptionStatus.CANCELED, today);
    }

    @Transactional
    public void modifySubscriptionStatusToActive(Long uId, SubscriptionStatus subscriptionStatus) {
        Subscription subscription = getSubscriptionByUserId(uId, subscriptionStatus);
        subscription.modifyStatus(SubscriptionStatus.ACTIVE);
    }
}
