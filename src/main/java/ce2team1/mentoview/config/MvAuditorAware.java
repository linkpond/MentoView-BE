package ce2team1.mentoview.config;

import ce2team1.mentoview.security.JwtTokenProvider;
import ce2team1.mentoview.security.dto.MvPrincipalDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;

@Component("MvAuditorAware")
@RequiredArgsConstructor
public class MvAuditorAware implements AuditorAware<String> {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("anonymousUser"); // 인증x anonymousUser
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof MvPrincipalDetails) { //폼 로그인
            return Optional.of(((MvPrincipalDetails) principal).getUsername());
        } else if (principal instanceof OAuth2User) { // OAuth 로그인
            Map<String, Object> attributes = ((OAuth2User) principal).getAttributes();
            return Optional.ofNullable((String) attributes.get("email"));
        }

        return getUserFromJwt();
    }

    private Optional<String> getUserFromJwt() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // "Bearer " 제거
            if (jwtTokenProvider.validateToken(token)) {
                return Optional.of(jwtTokenProvider.getEmailFromToken(token));
            }
        }
        return Optional.of("anonymousUser");
    }
}