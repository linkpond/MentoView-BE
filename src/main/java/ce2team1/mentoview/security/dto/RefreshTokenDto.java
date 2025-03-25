package ce2team1.mentoview.security.dto;

import ce2team1.mentoview.security.entity.RefreshToken;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for {@link ce2team1.mentoview.security.entity.RefreshToken}
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshTokenDto {
    private Long id;
    private String userEmail;
    private String refreshToken;
    private LocalDateTime expirationDate;
    private LocalDateTime maxExpirationDate;


    public static RefreshTokenDto of(String userEmail, String refreshToken, LocalDateTime expirationDate) {
        return new RefreshTokenDto(
                null,
                userEmail,
                refreshToken,
                expirationDate,
                expirationDate.plusDays(RefreshToken.MAX_EXPIRATION_DAYS)
        );
    }

    public static RefreshTokenDto toDto(RefreshToken refreshToken) {
        return RefreshTokenDto.builder()
                .id(refreshToken.getId())
                .userEmail(refreshToken.getUserEmail())
                .refreshToken(refreshToken.getRefreshToken())
                .expirationDate(refreshToken.getExpirationDate())
                .maxExpirationDate(refreshToken.getMaxExpirationDate())
                .build();
    }
}