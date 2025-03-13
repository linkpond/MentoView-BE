package ce2team1.mentoview.security;

import ce2team1.mentoview.entity.atrribute.Role;
import ce2team1.mentoview.security.dto.MvPrincipalDetails;
import ce2team1.mentoview.security.service.RefreshTokenService;
import ce2team1.mentoview.service.dto.UserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Slf4j
@RequiredArgsConstructor
public class MvOAuth2FormSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("‼️‼️‼️‼️‼️‼️‼️  OAuth2 로그인 성공: {}", authentication.getPrincipal().getClass());

        MvPrincipalDetails mvPrincipalDetails;
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {

            log.info("‼️‼️‼️‼️‼️‼️‼ OidcUser 로그인 처리");
            UserDto userDto = UserDto.byOAuth2User(oidcUser);
            mvPrincipalDetails = MvPrincipalDetails.of(userDto, oidcUser);

        } else if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {

            log.info("‼️‼️‼️‼️‼️‼️‼ OAuth2User 로그인 처리");
            UserDto userDto = UserDto.byOAuth2User(oAuth2User);
            mvPrincipalDetails = MvPrincipalDetails.of(userDto, oAuth2User.getAttributes());

        } else if (authentication.getPrincipal() instanceof UserDetails userDetails) {

            log.info("‼️‼️‼️‼️‼️‼️‼ UserDetails 로그인 처리");
            mvPrincipalDetails = (MvPrincipalDetails) userDetails;

        }else {
            throw new IllegalStateException("지원하지 않는 OAuth2 인증 타입입니다.");
        }
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(mvPrincipalDetails, null, mvPrincipalDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("️‼️‼️‼️‼️‼️‼  SecurityContext에 저장된 Authentication: {}", SecurityContextHolder.getContext().getAuthentication());


        String userEmail = mvPrincipalDetails.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();
        String temporaryToken = jwtTokenProvider.createTemporaryToken(userEmail, Role.toCode(role));// 2분

        String realRefreshToken = jwtTokenProvider.createRefreshToken(userEmail, Role.toCode(role));// 7일
        // 우리가 만든 refreshToken 디비로 저장
        refreshTokenService.updateOrAddRefreshToken(userEmail, realRefreshToken, jwtTokenProvider.getRefreshTokenExpiration());

        String tokenUrl = String.format("http://localhost:3000/google-login?token=%s", temporaryToken);
        response.sendRedirect(tokenUrl);


        log.info("jwtCookie{}" , temporaryToken);
        log.info("jwtCookie{} 드림" , temporaryToken);

    }

}


