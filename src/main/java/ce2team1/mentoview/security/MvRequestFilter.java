package ce2team1.mentoview.security;

import ce2team1.mentoview.entity.atrribute.Role;
import ce2team1.mentoview.security.dto.MvPrincipalDetails;
import ce2team1.mentoview.service.dto.UserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

/*
* 클라이언트 검증
* */
@Component
@RequiredArgsConstructor
public class MvRequestFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/oauth2/authorization") || path.startsWith("/api/login/oauth2/code");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String bearerToken = request.getHeader("Authorization");

        //요청에 토큰이 없어?
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authToken = bearerToken.substring(7).trim();

        if (jwtTokenProvider.isExpired(authToken)) {
            String emailFromToken = jwtTokenProvider.getEmailFromToken(authToken);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter writer = response.getWriter();
            writer.write("{\"error\":\"Expired JWT token\"}");
            writer.flush();
            return;

        }
        // 토큰 이름 찾아

        // 토큰 타입으로 찾아
        String type = jwtTokenProvider.getType(authToken);
        if (!type.equals("access") && !type.equals("temporary")) { // a도 아니고 b도 아닐떄
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter writer = response.getWriter();
            writer.write("{\"error\":\"invalid JWT token\"}");
            writer.flush();
            return;
        }

        String emailFromToken = jwtTokenProvider.getEmailFromToken(authToken);
        Role roleFromToken = jwtTokenProvider.getRoleFromToken(authToken);


        MvPrincipalDetails mvPrincipalDetails = MvPrincipalDetails.of(UserDto.of(emailFromToken, roleFromToken));
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(mvPrincipalDetails,null, mvPrincipalDetails.getAuthorities());
        // null, 비번
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }
}
