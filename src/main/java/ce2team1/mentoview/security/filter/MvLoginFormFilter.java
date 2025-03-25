package ce2team1.mentoview.security.filter;

import ce2team1.mentoview.security.dto.UserFormLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MvLoginFormFilter extends AbstractAuthenticationProcessingFilter {

    public MvLoginFormFilter(AuthenticationManager authenticationManager,
                             AuthenticationSuccessHandler successHandler,
                             AuthenticationFailureHandler failureHandler) {
        super(new AntPathRequestMatcher("/api/login", "POST"));
        setAuthenticationManager(authenticationManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);


        ObjectMapper objectMapper = new ObjectMapper();

        UserFormLogin userFormLogin = getUserFormLogin(cachingRequest, objectMapper);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userFormLogin.getEmail(), userFormLogin.getPassword());

        Authentication authenticate = getAuthenticationManager().authenticate(authentication);

        return authenticate;
    }

    private UserFormLogin getUserFormLogin(HttpServletRequest request, ObjectMapper objectMapper) {
        if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {

            throw new AuthenticationServiceException("이미 로그인된 사용자 입니다!");
        }

        String contentType = request.getContentType();
        if (!contentType.equals("application/json") || !contentType.toLowerCase().startsWith("application/json")) {

            throw new AuthenticationServiceException("유효하지않는 content type: " + contentType);
        }

        try {
            String requestBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);


            if (requestBody.isEmpty()) {

                throw new AuthenticationServiceException("요청 본문이 비어 있습니다.");
            }

            // JSON을 객체로 변환
            return objectMapper.readValue(requestBody, UserFormLogin.class);
        } catch (IOException e) {

            throw new AuthenticationServiceException("로그인 요청을 처리할 수 없습니다.");
        }

    }

}
