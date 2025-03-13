package ce2team1.mentoview.security.entity;

import ce2team1.mentoview.security.dto.RefreshTokenDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Builder(toBuilder = true)
@Getter
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    public static final long MAX_EXPIRATION_DAYS = 14;
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String userEmail;
    @Column(nullable = false)
    private String refreshToken;
    @Column(nullable = false)
    private LocalDateTime expirationDate;
    @Column(nullable = false)
    private LocalDateTime maxExpirationDate;

    public static RefreshToken of(String email, String refreshToken, LocalDateTime expirationDate) {
        return new RefreshToken(
                null,
                email,
                refreshToken,
                expirationDate,
                expirationDate.plusDays(MAX_EXPIRATION_DAYS)
        );
    }

    public static RefreshToken of(Long id, String email, String refreshToken, LocalDateTime expirationDate) {
        return new RefreshToken(
                id,
                email,
                refreshToken,
                expirationDate,
                expirationDate.plusDays(MAX_EXPIRATION_DAYS)
        );
    }
    public static RefreshToken toEntity(RefreshTokenDto tokenDto) {
        return RefreshToken.builder()
                .userEmail(tokenDto.getUserEmail())
                .refreshToken(tokenDto.getRefreshToken())
                .expirationDate(tokenDto.getExpirationDate())
                .maxExpirationDate(tokenDto.getExpirationDate().plusDays(MAX_EXPIRATION_DAYS))
                .build();

    }

    public void updateRefreshToken(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
        this.expirationDate = this.expirationDate.plusDays(7);
    }
}
