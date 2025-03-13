package ce2team1.mentoview.security;

import ce2team1.mentoview.entity.atrribute.Role;
import ce2team1.mentoview.repository.RefreshTokenRepository;
import ce2team1.mentoview.security.entity.RefreshToken;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenBatchJob {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;
    private static final int BATCH_SIZE = 50;


    //만료토큰삭제
    @Scheduled(cron = "0 0 0 * * ?") // 매일 00시 : 초,분,시,일,월,요일
    @Transactional
    public void deleteExpiredTokens() {
        log.info("Deleting expired tokens");
        try {
            int deletedRefreshToken
                    = refreshTokenRepository.deleteMaxExpirationDate(LocalDateTime.now());
            if (deletedRefreshToken == 0) {
                log.info("만료된 토큰 없음");
            }
            log.info("Deleted expired tokens: " + deletedRefreshToken);
            eventPublisher.publishEvent(new RefreshTokenDeletedEvent(LocalDateTime.now()));
            log.info("작업 완료");
        } catch (Exception e) {
            log.error("만료된 토큰 삭제 중 오류 발생 {}", e.getMessage(),e);
        }
    }


    @TransactionalEventListener
    public void rotateRefreshToken(RefreshTokenDeletedEvent deletedEvent) {
        log.info("만료 토큰 삭제 완료 ==> Start Rotating refresh token!!");

        List<RefreshToken> expiringRefreshTokens = refreshTokenRepository.findExpiringRefreshTokens(LocalDateTime.now().minusMinutes(1));
        int totalExpiring = expiringRefreshTokens.size();
        int updatedTokens = 0;
        int failedTokens = 0;

        int counter = 0;

        try {
            for (RefreshToken refreshToken : expiringRefreshTokens) {
                String newRefreshToken = jwtTokenProvider.createRefreshToken(refreshToken.getUserEmail(), Role.USER);
                refreshToken.updateRefreshToken(newRefreshToken);

                updatedTokens++;

                counter++;
                if (counter % BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        } catch (Exception e) {
            failedTokens++;
            log.error("리프레시 토큰 갱신 중 오류발생 {} "+e.getMessage(), e);
        }
        log.info("Rotating refresh token!! ==>  만료 토큰 갱신 완료 ");
        log.info("조회된 만료 예정 토큰 : {}, 갱신 성공 : {}, 갱신 실패 : {}", totalExpiring , updatedTokens, failedTokens);
    }

}
