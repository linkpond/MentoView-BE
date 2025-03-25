package ce2team1.mentoview.security.controller;

import ce2team1.mentoview.controller.dto.response.UserResp;
import ce2team1.mentoview.entity.atrribute.Role;
import ce2team1.mentoview.security.service.JwtTokenProvider;
import ce2team1.mentoview.security.dto.MvPrincipalDetails;
import ce2team1.mentoview.service.UserService;
import ce2team1.mentoview.service.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth User API", description = "Auth")
@RequiredArgsConstructor
public class OAuth2Controller {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;


    @Operation(summary = "Auth", description = "Auth Token && user data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OAuth2 user"),
            @ApiResponse(responseCode = "400", description = "NOT_FOUND")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/me")
    public ResponseEntity<UserResp> getUserInfo (@AuthenticationPrincipal MvPrincipalDetails mvPrincipalDetails,
                                             HttpServletRequest request) {

        log.info("mvPrincipalDetails: {}", mvPrincipalDetails);
        UserDto dto = mvPrincipalDetails.getUserDto();
        System.out.println("dto: " + dto);

        String existingToken = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .orElseThrow(() -> new AuthenticationServiceException("미인증 유저"));
        String token = existingToken.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UserResp.of(null, "만료된 토큰"));
        }

        if (!"temporary".equals(jwtTokenProvider.getType(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(UserResp.of(null, "임시토큰이 아님"));
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        Role role = jwtTokenProvider.getRoleFromToken(token);

        UserDto userDto = userService.findByEmail(email);
        String accessToken = jwtTokenProvider.createAccessToken(email, role);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        log.info("headers: {}", headers);

        UserResp userResp = UserResp.of(userDto, "사용자 정보 조회 성공");

        log.info("userResp: {}", userResp);
        
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(userResp);
    }

}
