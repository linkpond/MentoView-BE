package ce2team1.mentoview.security.filter;

import ce2team1.mentoview.entity.User;
import ce2team1.mentoview.entity.atrribute.UserStatus;
import ce2team1.mentoview.repository.UserRepository;
import ce2team1.mentoview.security.dto.MvPrincipalDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeletedUserFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof MvPrincipalDetails principalDetails) {
            Long userId = principalDetails.getUserId();
            UserStatus userStatus = userRepository.findById(userId).map(User::getStatus)
                    .orElse(null);// 유저 업으면 null
            if (userStatus == UserStatus.DELETED) {
                log.warn("탈퇴 유저 {}", userId);
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "탈퇴유저");
                return;
            }
            if (userStatus == UserStatus.SUSPENDED ) {
                log.warn("제한된 유저 {}", userId);
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "접근 제한된 계정");
                return;
            }
        }
        filterChain.doFilter(request, response);


    }
}
