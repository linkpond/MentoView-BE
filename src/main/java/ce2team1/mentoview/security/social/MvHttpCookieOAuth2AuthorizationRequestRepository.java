package ce2team1.mentoview.security.social;

import ce2team1.mentoview.utils.security.MvCookieUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Slf4j
public class MvHttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_authorization_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";


    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = MvCookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> {
                    log.info("OAuth2AuthorizationRequest의 쿠키: {}", cookie.getValue());
                    return MvCookieUtils.decodingCookie(cookie, OAuth2AuthorizationRequest.class);
                })
                .orElse(null);

        if (oAuth2AuthorizationRequest == null) {
            log.error("쿠키에서 OAuth2AuthorizationRequest 못 찾음 -> default라도 써아");
            return null;
        }

        if (StringUtils.isBlank(oAuth2AuthorizationRequest.getRedirectUri())) {
            log.warn("redirectUri 없음. 기본값 사용");
            return OAuth2AuthorizationRequest.from(oAuth2AuthorizationRequest)
                    .redirectUri("http://localhost:8080/login/oauth2/code/google")
                    .build();
        }

        log.info("‼️ [OAuth2 요청 로드] Authorization Request: {}", oAuth2AuthorizationRequest);
        log.info("‼️ [OAuth2 요청 로드] redirectUri: {}", oAuth2AuthorizationRequest.getRedirectUri());
        return oAuth2AuthorizationRequest;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
//        if (authorizationRequest == null) {
//            removeAuthorizationRequest(request, response);
//            return;
//        }

        log.info("저장할 OAuth2AuthorizationRequest: {}", authorizationRequest.getRedirectUri());

        OAuth2AuthorizationRequest defaultUri = OAuth2AuthorizationRequest.from(authorizationRequest)
                .redirectUri(authorizationRequest.getRedirectUri() != null ? authorizationRequest.getRedirectUri() : "http://localhost:8080/login/oauth2/code/google")
                .build();


        // 요청 저장
        MvCookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, MvCookieUtils.incodingCookie(defaultUri), 180);

        // 로그인후 리디렛션
        String afterLoginRedirectUri = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        log.info("요청에서 가져온 redirect_uri: {}", afterLoginRedirectUri);

        if (StringUtils.isNotBlank(afterLoginRedirectUri)) {
            MvCookieUtils.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, afterLoginRedirectUri, 180);
            log.info("✅ 추가 저장된 redirect_uri 쿠키: {}", afterLoginRedirectUri);
        } else {
            log.warn("⚠️ 요청에 redirect_uri가 없음. 저장 안 됨.");
        }


    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {

        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = this.loadAuthorizationRequest(request);
        MvCookieUtils.deleteCookie(request,response,OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        MvCookieUtils.deleteCookie(request,response,REDIRECT_URI_PARAM_COOKIE_NAME);
        return oAuth2AuthorizationRequest;
    }
}
