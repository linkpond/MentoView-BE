package ce2team1.mentoview.controller;


import ce2team1.mentoview.controller.dto.request.UserCreateForm;
import ce2team1.mentoview.controller.dto.request.UserMypage;
import ce2team1.mentoview.controller.dto.request.UserPasswordCreate;
import ce2team1.mentoview.controller.dto.request.UserPasswordModify;
import ce2team1.mentoview.controller.dto.response.FormUserResp;
import ce2team1.mentoview.controller.dto.response.UserResp;
import ce2team1.mentoview.exception.UserException;
import ce2team1.mentoview.security.dto.MvPrincipalDetails;
import ce2team1.mentoview.security.service.JwtTokenProvider;
import ce2team1.mentoview.security.service.RefreshTokenService;
import ce2team1.mentoview.service.ResumeService;
import ce2team1.mentoview.service.SubscriptionService;
import ce2team1.mentoview.service.UserService;
import ce2team1.mentoview.service.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.header.Header;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
@Tag(name = "User API", description = "회원가입, myPage API")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ResumeService resumeService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Form 회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/signup/form")
    public ResponseEntity<FormUserResp> signup(@RequestBody @Validated UserCreateForm userCreateForm) {
        UserDto userDto = UserDto.builder()
                .name(userCreateForm.getName())
                .email(userCreateForm.getEmail())
                .password(userCreateForm.getPassword())
                .build();
        FormUserResp formUserResp = FormUserResp.of(userService.createUser(userDto), "가입완료");

        return ResponseEntity.ok(formUserResp);
    }

    @Operation(summary = "마이페이지", description = "마이페이지 접근")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증된 유저"),
            @ApiResponse(responseCode = "400", description = "인증 없음")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/mypage")
    public ResponseEntity<UserResp> mypage (@AuthenticationPrincipal MvPrincipalDetails mvPrincipalDetails,
                                            @Valid @RequestBody UserMypage userMypage) {
        Long userId = mvPrincipalDetails.getUserId();
        String password = userMypage.getPassword();

        UserDto userDto = userService.accessMyPage(userId, password);
        UserResp userResp = UserResp.of(userDto, "인증된 유저");

        return ResponseEntity.ok(userResp);
    }


    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/mypage/password")
    public ResponseEntity<String> modifyPassword(@AuthenticationPrincipal MvPrincipalDetails mvPrincipalDetails, @Valid @RequestBody UserPasswordModify userPasswordModify) {
        Long userId = mvPrincipalDetails.getUserId();
        if (!userPasswordModify.getAfterPassword().equals(userPasswordModify.getAfterPasswordCheck())) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        userService.changePassword(userId, userPasswordModify.getBeforePassword(),userPasswordModify.getAfterPassword());

        return ResponseEntity.ok("비밀번호 변경 성공");


    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "권한 없음")
    })
    @DeleteMapping("/mypage/delete/{id}")
    public ResponseEntity<String> userDelete(@AuthenticationPrincipal MvPrincipalDetails mvPrincipalDetails,
                                             @PathVariable Long id) {

        Long userId = mvPrincipalDetails.getUserId();

        if (!userId.equals(id)) {
            throw new UserException( "삭제할 권한이 없습니다.", HttpStatus.NOT_FOUND);
        }
        userService.softDelete(userId);
        resumeService.hardDeleteResume(userId);
        refreshTokenService.deleteRefreshToken(mvPrincipalDetails.getName());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName().equals(mvPrincipalDetails.getName())) {
            SecurityContextHolder.clearContext(); // 강제 로그아웃
        }

        return ResponseEntity.ok("삭제완료");
    }

    @Operation(summary = "social 비밀번호 생성", description = "social 사용자의 비밀번호를 생성합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "social 비밀번호 생성"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/social/password")
    public ResponseEntity<UserResp> createPassword(@AuthenticationPrincipal MvPrincipalDetails mvPrincipalDetails, @Valid @RequestBody UserPasswordCreate userPasswordCreate) {
        Long userId = mvPrincipalDetails.getUserId();
        if (!userPasswordCreate.getPassword().equals(userPasswordCreate.getPasswordCheck())) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        UserDto password = userService.createPassword(userId, userPasswordCreate.getPassword());
        UserResp resp = UserResp.of(password, "비밀번호 변경 성공");

        return ResponseEntity.status(HttpStatus.OK).body(resp);

    }


    /*
    *  String accessToken = jwtTokenProvider.createAccessToken(password.getEmail() , password.getRole());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("https://mentoview.site/"));
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        return ResponseEntity.status(HttpStatus.FOUND)
                .headers(headers).body(resp);

    *
    * */




}

