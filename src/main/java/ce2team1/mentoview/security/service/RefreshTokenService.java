package ce2team1.mentoview.security.service;

import ce2team1.mentoview.repository.RefreshTokenRepository;
import ce2team1.mentoview.security.dto.RefreshTokenDto;
import ce2team1.mentoview.security.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public String getRefreshToken(String email) {
        return refreshTokenRepository.findByUserEmail(email).map(
                        refreshToken -> refreshToken.getRefreshToken())
                .orElseThrow(() -> new AuthenticationServiceException( "No refresh token found for email: " + email));
    }

    @Transactional(readOnly = false)
    public void updateOrAddRefreshToken(String userEmail, String realRefreshToken, Long expiration) {

        log.info("‼️‼️‼️‼️‼️‼️‼️userEmail 확인 = {}", userEmail); // 메서드 호출 전

        LocalDateTime expirationDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(expiration), ZoneId.of("Asia/Seoul")); //테이블용 시간

        RefreshTokenDto tokenDto = RefreshTokenDto.of(userEmail, realRefreshToken, expirationDate); //RefreshDto 토큰 객체

        Optional<RefreshToken> existToken = refreshTokenRepository.findByUserEmail(userEmail); // DB에 Refresh 토큰 객체를 email로 찾음

        if (!existToken.isPresent()) { // 디비에 객체 없어?
            RefreshToken newRefreshToken = refreshTokenRepository.save(RefreshToken.toEntity(tokenDto));

        }  else {// 디비에 객체 있어?
            RefreshToken updateRefreshToken = existToken.get().toBuilder()
                    .refreshToken(realRefreshToken)
                    .expirationDate(expirationDate)
                    .build();
            RefreshToken existedToken = refreshTokenRepository.save(updateRefreshToken);
        }
    }
    @Transactional(readOnly = false)
    public void deleteRefreshToken(String email) {
        refreshTokenRepository.deleteByUserEmail(email);
    }

    public RefreshTokenDto findByEmail(String email) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserEmail(email).orElseThrow(
                () -> new AuthenticationServiceException("No refresh token found for email: " + email));
        return RefreshTokenDto.toDto(refreshToken);

    }


}
