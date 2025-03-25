package ce2team1.mentoview.service;


import ce2team1.mentoview.controller.dto.response.PaymentResp;
import ce2team1.mentoview.entity.Payment;
import ce2team1.mentoview.entity.Subscription;
import ce2team1.mentoview.entity.atrribute.SubscriptionStatus;
import ce2team1.mentoview.service.dto.PaymentCheckDto;
import ce2team1.mentoview.repository.PaymentRepository;
import ce2team1.mentoview.service.dto.PaymentDto;
import ce2team1.mentoview.service.dto.SubscriptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SubscriptionService subscriptionService;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void createPayment(PaymentCheckDto paymentCheckDto) {

        // 구독 생성 후 결제 저장

        // 사용자에게 활성화된 구독(status == 'ACTIVE')이 존재하는지 확인
        Long subId = subscriptionService.checkSubscription(Long.valueOf(paymentCheckDto.getCustomer().getId()));

        SubscriptionDto subscription;
        if (subId != null){
            subscription = subscriptionService.modifyEndDateAndNextBillingDate(subId, paymentCheckDto.getPaidAt());
        } else {
            subscription = subscriptionService.createSubscription(paymentCheckDto);
        }

        Payment payment = PaymentDto.checkToDto(paymentCheckDto, subscription.getSubId()).toEntity(subscriptionService.getSubscriptionByUserId(subscription.getUserId(), SubscriptionStatus.ACTIVE));
        paymentRepository.save(payment);
    }

    public List<PaymentResp> getPayment(Long subId) {

        return paymentRepository.findBySubscription_SubId(subId)
                .stream()
                .map(payment -> {
                    PaymentResp resp = PaymentResp.toResp(PaymentDto.toDto(payment));
                    return resp;
                })
                .collect(Collectors.toList());

    }

}
