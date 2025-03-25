package ce2team1.mentoview.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

@Slf4j
public class MvOAuth2FormFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 로그인 실패: {}", exception.getMessage(), exception);
        log.error("요청 쿼리 파라미터: {}", request.getQueryString()); // 요청 파라미터 확인
        log.error("🔍 Google에서 보낸 redirect_uri: {}", request.getParameter("redirect_uri"));
        log.error("🔍 설정된 redirect_uri: {}", "http://localhost:8080/login/oauth2/code/google");

        exception.getStackTrace();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter()
                .write("{\"error\":\"소셜 로그인 실패\"," + "\n"+
                        "\"message\":\"" + exception.getMessage()+ "\"}");
        response.getWriter().flush();



    }
}
