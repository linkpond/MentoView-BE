package ce2team1.mentoview.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class WebhookFilter extends OncePerRequestFilter {
    private final List<String> allowedIps = List.of("52.78.5.241");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 요청 경로 확인
        String requestUri = request.getRequestURI();

        if (!requestUri.startsWith("/api/webhook/")) {
            // 해당 경로가 아니면 필터를 통과
            chain.doFilter(request, response);
            return;
        }

        // X-Forwarded-For 헤더에서 클라이언트 IP 주소 가져오기
        String remoteAddr = request.getHeader("X-Forwarded-For");

        // X-Forwarded-For가 없거나 비어있으면 기본 remoteAddr 가져오기
        if (remoteAddr == null || remoteAddr.isEmpty()) {
            remoteAddr = request.getRemoteAddr();
        } else {
            // X-Forwarded-For 헤더는 여러 IP 주소를 포함할 수 있으므로,
            // 가장 왼쪽의 IP 주소를 가져옵니다.
            String[] ipAddresses = remoteAddr.split(",");
            remoteAddr = ipAddresses[0].trim(); // 첫 번째 IP 주소
        }

        System.out.println("Webhook 요청 IP: " + remoteAddr);

        if (!allowedIps.contains(remoteAddr)) {
            System.out.println("허용되지 않은 IP 접근 차단: " + remoteAddr);
            response.getWriter().write("Forbidden");
            return;
        }

        chain.doFilter(request, response);
    }
}






