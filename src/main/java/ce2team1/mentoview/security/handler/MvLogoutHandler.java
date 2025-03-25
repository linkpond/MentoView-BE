package ce2team1.mentoview.security.handler;

import ce2team1.mentoview.exception.ServiceException;
import ce2team1.mentoview.security.service.JwtTokenProvider;
import ce2team1.mentoview.security.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class MvLogoutHandler implements LogoutHandler {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        try {
            String authorization = request.getHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                extracted(response, HttpServletResponse.SC_BAD_REQUEST, "{\"code\" : \"INVALID_REQUEST\", \"message\" : \" 토큰이 없습니다\"}");
                return;
            }
            if (authorization != null && authorization.startsWith("Bearer ")) {
                authorization = authorization.substring(7);
                String email = jwtTokenProvider.getEmailFromToken(authorization);
                refreshTokenService.deleteRefreshToken(email);
                extracted(response, HttpServletResponse.SC_ACCEPTED, "{\"code\" : \"LOGOUT_SUCCESS\", \"message\" : \" 로그아웃 성공, Access Token 삭제 요청\"}");
                return;
            }
        } catch (ServiceException e) {
            extracted(response, HttpServletResponse.SC_BAD_REQUEST, "{\"code\":\"TOKEN_NOT_FOUND\", \"message\":\"삭제할 토큰이 없습니다.\"}");
        } catch (Exception e) {
            extracted(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "{\"code\":\"LOGOUT_ERROR\", \"message\":\"로그아웃 처리 중 오류 발생\"}");
        }
    }
    private void extracted(HttpServletResponse response, int status, String message) {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(message);
            writer.flush();
        } catch (IOException e) {
            log.error("{}",e.getMessage(),e);
        }
    }
}
