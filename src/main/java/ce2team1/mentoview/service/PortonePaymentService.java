package ce2team1.mentoview.service;

import ce2team1.mentoview.controller.dto.request.PaymentCreate;
import ce2team1.mentoview.entity.Subscription;
import ce2team1.mentoview.entity.User;
import ce2team1.mentoview.entity.atrribute.SubscriptionStatus;
import ce2team1.mentoview.exception.ServiceException;
import ce2team1.mentoview.exception.SubscriptionException;
import ce2team1.mentoview.repository.UserRepository;
import ce2team1.mentoview.service.dto.BillingKeyCheckDto;
import ce2team1.mentoview.service.dto.PaymentCheckDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PortonePaymentService {

    private final WebClient webClient;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${IMP_API_KEY}")
    private String portoneApiSecret; // PortOne API 시크릿
    private String baseUrl = "https://api.portone.io";

    @Value("${NOTIFICATION_URL}")
    private String notificationUrl; // 포트원이 웹훅 전달할 URL

    public boolean checkPayment(PaymentCreate paymentCreate) throws JsonProcessingException {

        String encodedPaymentId = URLEncoder.encode(paymentCreate.getData().getPaymentId(), StandardCharsets.UTF_8);

        PaymentCheckDto paymentCheckDto = webClient.get()
                .uri(baseUrl + "/payments/" + encodedPaymentId)
                .headers(headers -> headers.set("Authorization", "PortOne " + portoneApiSecret))
                .retrieve()
                .bodyToMono(PaymentCheckDto.class)
                .block(); // 동기 방식 호출

        // 검증 로직 분리
        validatePayment(paymentCheckDto);
        return true;
    }

    private void validatePayment(PaymentCheckDto paymentCheckDto) throws JsonProcessingException {
        if (paymentCheckDto == null) {
            throw new RuntimeException("Invalid payment response");
        }

        // 결제 금액 및 상태 확인
        if (paymentCheckDto.getAmount().getTotal().compareTo(BigDecimal.valueOf(10000.0)) == 0 &&
                "PAID".equals(paymentCheckDto.getStatus())) {
            // 유효한 결제일 경우, 구독 생성 및 결제 저장
            paymentService.createPayment(paymentCheckDto);
            // 다음 결제 예약
            schedulePayment(Long.valueOf(paymentCheckDto.getCustomer().getId()), paymentCheckDto.getBillingKey(), paymentCheckDto.getPaidAt(), null);
        } else {
            throw new RuntimeException("Payment amount mismatch detected");
        }
    }

    private void schedulePayment(Long uId, String billingKey, String paidAt, String willPayAt) throws JsonProcessingException {

        String paymentId = "payment-" + UUID.randomUUID().toString();
        String encodedPaymentId = URLEncoder.encode(paymentId, StandardCharsets.UTF_8);


        OffsetDateTime paymentDateByPaidAt = null;
        OffsetDateTime paymentDateByWillPayAt = null;
        if (paidAt != null) {
            paymentDateByPaidAt = OffsetDateTime.parse(paidAt)
                    .plusDays(31) // 31일 추가
                    .withOffsetSameInstant(ZoneOffset.of("+09:00")); // KST로 설정
        } else {
            paymentDateByWillPayAt = OffsetDateTime.parse(willPayAt)
                    .withOffsetSameInstant(ZoneOffset.of("+09:00"));
        }

        OffsetDateTime paymentDate = willPayAt == null ? paymentDateByPaidAt : paymentDateByWillPayAt;
        System.out.println(paymentDate);

        try {
            String response = webClient.post()
                    .uri(baseUrl + "/payments/{paymentId}/schedule", encodedPaymentId)
                    .header(HttpHeaders.AUTHORIZATION, "PortOne " + portoneApiSecret)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(createRequestBodyForSchedulingPayment(uId, billingKey, paymentDate))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println(response);
            String scheduleId = response.split("\"schedule\":\\{\"id\":\"")[1].split("\"")[0];
            subscriptionService.initPaymentScheduleIdAndPaymentId(uId, paymentId, scheduleId);

        } catch (Exception e) {
            throw new SubscriptionException(e.getMessage() + "결제 처리 중 문제가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String createRequestBodyForSchedulingPayment(Long uId, String billingKey, OffsetDateTime dateTime) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("billingKey", billingKey);
        paymentDetails.put("orderName", "월간 이용권 정기결제");

        Map<String, Object> customer = new HashMap<>();
        customer.put("id", String.valueOf(uId));
        paymentDetails.put("customer", customer);

        Map<String, Integer> amount = new HashMap<>();
        amount.put("total", 10000);
        paymentDetails.put("amount", amount);

        paymentDetails.put("currency", "KRW");

        List<String> noticeUrls = Arrays.asList(notificationUrl);
        paymentDetails.put("noticeUrls", noticeUrls);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("payment", paymentDetails);

        String formattedTime = dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        requestBody.put("timeToPay", formattedTime);

        String response = objectMapper.writeValueAsString(requestBody);
        System.out.println(response);

        return response;
    }

    public BillingKeyCheckDto checkBillingKey(String billingKey) throws JsonProcessingException {

        String encodedBillingKey = URLEncoder.encode(billingKey, StandardCharsets.UTF_8);

        BillingKeyCheckDto response = webClient.get()
                .uri(baseUrl + "/billing-keys/{billingKey}", encodedBillingKey)
                .headers(headers -> headers.set("Authorization", "PortOne " + portoneApiSecret))
                .retrieve()
                .bodyToMono(BillingKeyCheckDto.class)
                .block(); // 동기 방식 호출 (비동기 필요 시 .subscribe() 사용)

        System.out.println(response.getCustomer().getId());

        validateBillingKey(response);

        return response;
    }

    private void validateBillingKey(BillingKeyCheckDto billingKeyCheckDto) throws JsonProcessingException {
        if (billingKeyCheckDto == null || "DELETED".equals(billingKeyCheckDto.getStatus())) {
            throw new RuntimeException("Invalid billingkey response");
        }
        else {
            userService.setBillingKey(Long.valueOf(billingKeyCheckDto.getCustomer().getId()), billingKeyCheckDto.getBillingKey());
        }
    }

    public void createPayment(Long uId) throws JsonProcessingException {

        // 포트원 서버로 빌링키 결제 요청
        User user = userRepository.findById(uId).orElseThrow();
        String billingKey = user.getBillingKey();

        // paymentId 생성
        String paymentId = "payment-" + UUID.randomUUID().toString();
        String encodedPaymentId = URLEncoder.encode(paymentId, StandardCharsets.UTF_8);

        try {
            String response = webClient.post()
                    .uri(baseUrl + "/payments/{paymentId}/billing-key", encodedPaymentId)
                    .header(HttpHeaders.AUTHORIZATION, "PortOne " + portoneApiSecret)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(createRequestBodyForCreatingPayment(uId, billingKey))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println(response);
        } catch (Exception e) {
            throw new SubscriptionException(e.getMessage() + "결제 요청이 실패하였습니다. 잠시 후에 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    private String createRequestBodyForCreatingPayment(Long uId, String billingKey) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("billingKey", billingKey);
        requestBody.put("orderName", "월간 이용권 정기결제");

        Map<String, Object> customer = new HashMap<>();
        customer.put("id", String.valueOf(uId));
        requestBody.put("customer", customer);

        Map<String, Integer> amount = new HashMap<>();
        amount.put("total", 10000);
        requestBody.put("amount", amount);

        requestBody.put("currency", "KRW");

        List<String> noticeUrls = Arrays.asList(notificationUrl);
        requestBody.put("noticeUrls", noticeUrls);

        String response = objectMapper.writeValueAsString(requestBody);
        System.out.println(response);

        return response;
    }

    public void cancelScheduling(Long uId) throws JsonProcessingException {

        String billingKey = userService.getBillingKey(uId);

        try {
            String response = webClient.method(HttpMethod.DELETE)
                    .uri(baseUrl + "/payment-schedules")
                    .headers(headers -> headers.set("Authorization", "PortOne " + portoneApiSecret))
                    .bodyValue(createRequestBodyForCancelScheduling(billingKey))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println(response);
        } catch (Exception e) {
            throw new SubscriptionException(e.getMessage() + "구독 취소 요청이 실패하였습니다. 잠시 후에 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        deleteBillingKey(billingKey);
        userService.setBillingKey(uId, null);

    }

    private String createRequestBodyForCancelScheduling(String billingKey) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("billingKey", billingKey);

        String response = objectMapper.writeValueAsString(requestBody);
        System.out.println(response);

        return response;
    }

    public void deleteBillingKey(String billingKey) throws JsonProcessingException {

        String encodedBillingKey = URLEncoder.encode(billingKey, StandardCharsets.UTF_8);


        try {
            String response = webClient.method(HttpMethod.DELETE)
                    .uri(baseUrl + "/billing-keys/" + encodedBillingKey)
                    .headers(headers -> headers.set("Authorization", "PortOne " + portoneApiSecret))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println(response);

        } catch (Exception e) {
            throw new SubscriptionException(e.getMessage() + "빌링키 삭제 요청이 실패하였습니다. 잠시 후에 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    public void processChangingBillingKey(BillingKeyCheckDto billingKeyCheckDto) throws JsonProcessingException {

        // 구독 조회
        Subscription subscription = subscriptionService.getSubscriptionByUserId(Long.valueOf(billingKeyCheckDto.getCustomer().getId()), SubscriptionStatus.ACTIVE);

        // 구독의 portoneScheduleId값을 통해 결제 예약 조회해서 이전 예약 시간 가져옴
        String timeToPay = getScheduling(subscription.getPortoneScheduleId());

        // 빌링키로 기존 결제 예약 취소
        cancelScheduling(Long.valueOf(billingKeyCheckDto.getCustomer().getId()));

        // 새로운 빌링키로 다시 결제 예약
        schedulePayment(Long.valueOf(billingKeyCheckDto.getCustomer().getId()), billingKeyCheckDto.getBillingKey(), null, timeToPay);

        // 빌링키 변경
//        subscriptionService.modifyBillingKey(subscription.getSubId(), billingKeyCheckDto.getBillingKey());
        userService.setBillingKey(Long.valueOf(billingKeyCheckDto.getCustomer().getId()), billingKeyCheckDto.getBillingKey());

    }

    private String getScheduling(String portoneScheduleId) throws JsonProcessingException {

        String encodedScheduleId = URLEncoder.encode(portoneScheduleId, StandardCharsets.UTF_8);

        String response = webClient.get()
                .uri(baseUrl + "/payment-schedules/" + encodedScheduleId)
                .headers(headers -> headers.set("Authorization", "PortOne " + portoneApiSecret))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> System.out.println("결제 예약 조회 실패: " + error.getMessage()))
                .block(); // 동기 방식 호출


        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);

        String timeToPay = rootNode.get("timeToPay").asText();
        return timeToPay;

    }

    public void processSubscriptionReactivation(Long uId) throws JsonProcessingException {

        Subscription subscription = subscriptionService.getSubscriptionByUserId(uId, SubscriptionStatus.CANCELED);

        // 기존 nextBillingDate로 다시 결제 예약
        schedulePayment(uId, userService.getBillingKey(uId), null, subscription.getNextBillingDate().atStartOfDay(ZoneOffset.UTC).toString());
        // 구독 상태 ACTIVE로 변경
        subscriptionService.modifySubscriptionStatusToActive(uId, SubscriptionStatus.CANCELED);

    }

    public void processFreeTierSubscription(Long uId) throws JsonProcessingException {
        String billingKey = userService.getBillingKey(uId);

        subscriptionService.createFreeTierSubscription(uId);

        schedulePayment(uId, billingKey, null, LocalDate.now().plusDays(31).atStartOfDay(ZoneOffset.UTC).toString());

    }
}
