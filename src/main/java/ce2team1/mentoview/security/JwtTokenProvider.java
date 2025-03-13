package ce2team1.mentoview.security;

import ce2team1.mentoview.entity.atrribute.Role;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
* Jwt : 0.12.x버전
 * */
@Getter
@Component
public class JwtTokenProvider {

    private SecretKey secretKey;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
    private long temporaryTokenExpiration;

    private static final String TEMPORARY = "temporary";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    //JwtTokenProvider 생성자
    public JwtTokenProvider(
            @Value("${spring.jwt.secret}") String secret,
            @Value("${access-token-expiration}") long accessTokenExpiration,
            @Value("${refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${temporary-token-expiration}") long temporaryTokenExpiration
    ){

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.temporaryTokenExpiration = temporaryTokenExpiration;
    }

    //TemporaryToken
    public String createTemporaryToken(String email, Role role) {
        return createToken(TEMPORARY, email, role, temporaryTokenExpiration);
    }

    //AccessToken
    public String createAccessToken(String email, Role role) {
        return createToken(ACCESS, email, role, accessTokenExpiration);
    }
    //RefreshToken
    public String createRefreshToken(String email, Role role) {
        return createToken(REFRESH ,email, role, refreshTokenExpiration);
    }


    // 토큰 검증 메서드 : 토큰을 인자로 받음 ,java꺼
    public boolean validateToken(String token) { // 서명 검증
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) { // 이멜검증
        if (!validateToken(token)) {
            throw new SecurityException("Invalid JWT Token");
        }
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("email").toString();
    }

    public Role getRoleFromToken(String token) { //role검증
        if (!validateToken(token)) {
            throw new SecurityException("Invalid JWT Token");
        }
        String role = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("role").toString();
        return Role.toCode(role);
    }


    public Boolean isExpired(String token) { // 만료
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                    .getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getType(String token) { //타입검
        if (!validateToken(token)) {
            throw new SecurityException("Invalid JWT Token");
        }
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
                .get("type").toString();
    }


    // 토큰 생성!
    private  String createToken(String type ,String email, Role role, Long expiration) {
        return Jwts.builder()
                .claim("type", type)
                .claim("email", email)
                .claim("role", role.getCode())
                .issuedAt(new Date(System.currentTimeMillis())) //시간
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }
}
