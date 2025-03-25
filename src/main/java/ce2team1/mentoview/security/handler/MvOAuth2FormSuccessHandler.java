package ce2team1.mentoview.security.handler;

import ce2team1.mentoview.entity.atrribute.Role;
import ce2team1.mentoview.security.dto.LoginType;
import ce2team1.mentoview.security.dto.MvPrincipalDetails;
import ce2team1.mentoview.security.service.JwtTokenProvider;
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
        if (authentication.getPrincipal() instanceof MvPrincipalDetails principalDetails) {
            log.info("‼️‼️‼️‼️ MvPrincipalDetails 로그인 처리");
            mvPrincipalDetails = principalDetails;

        } else if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            log.info("‼️‼️‼️‼️‼️‼️‼️ OidcUser 로그인 처리");
            UserDto userDto = UserDto.byOAuth2User(oidcUser);
            mvPrincipalDetails = MvPrincipalDetails.of(userDto, LoginType.OIDC);

        } else if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            log.info("‼️‼️‼️‼️‼️‼️‼️ OAuth2User 로그인 처리");
            UserDto userDto = UserDto.byOAuth2User(oAuth2User);
            mvPrincipalDetails = MvPrincipalDetails.of(userDto, LoginType.OAUTH2);

        } else {
            throw new IllegalStateException("지원하지 않는 인증 타입입니다: " + authentication.getPrincipal().getClass());
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(mvPrincipalDetails, null, mvPrincipalDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.info("️‼️‼️‼️‼️‼️‼  SecurityContext에 저장된 Authentication: {}", SecurityContextHolder.getContext().getAuthentication());

        String userEmail = mvPrincipalDetails.getName();
        log.info("‼️‼️‼11111111‼️‼️‼️‼️userEmail 확인 = {}", userEmail);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();
        log.info("‼️‼️‼️222‼️‼️‼️‼️userEmail 확인 = {}", userEmail);
        String temporaryToken = jwtTokenProvider.createTemporaryToken(userEmail, Role.toCode(role));// 2분

        String realRefreshToken = jwtTokenProvider.createRefreshToken(userEmail, Role.toCode(role));// 7일
        // 우리가 만든 refreshToken 디비로 저장
        log.info("‼️‼️‼️333333‼️‼️‼️‼️userEmail 확인 = {}", userEmail);
        refreshTokenService.updateOrAddRefreshToken(userEmail, realRefreshToken, jwtTokenProvider.getRefreshTokenExpiration());

        String tokenUrl;        //= String.format("http://localhost:3000/google-login?token=%s", temporaryToken);

        if (mvPrincipalDetails.getLoginType() != LoginType.FORM && mvPrincipalDetails.getPassword().isEmpty() ) {
            tokenUrl = String.format("https://mentoview.site/mv-login?token=%s&ndg=%s", temporaryToken, "fa");
        } else {
            tokenUrl = String.format("https://mentoview.site/mv-login?token=%s&ndg=%s", temporaryToken, "tu");
        }


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.sendRedirect(tokenUrl);

        log.info("jwt{}" , temporaryToken);
        log.info("jwt{} 드림" , temporaryToken);
    }

}


