package ce2team1.mentoview.security.service;

import ce2team1.mentoview.entity.User;
import ce2team1.mentoview.entity.atrribute.Role;
import ce2team1.mentoview.entity.atrribute.UserStatus;
import ce2team1.mentoview.repository.UserRepository;
import ce2team1.mentoview.security.dto.GoogleOAuth2Response;
import ce2team1.mentoview.security.dto.LoginType;
import ce2team1.mentoview.security.dto.MvPrincipalDetails;
import ce2team1.mentoview.security.dto.OAuth2ResponseSocial;
import ce2team1.mentoview.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MvOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("✅ ‼️‼️OAuth2UserRequest 정보: {}", userRequest);
        log.info("✅ ‼️‼️OAuth2UserRequest 정보.getRedirectUri: {}", userRequest.getClientRegistration().getRedirectUri());
        log.info("✅ ‼️‼️OAuth2 Access Token: {}", userRequest.getAccessToken().getTokenValue());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("user: {}", oAuth2User);
        log.info("✅ OAuth2User 정보: {}", oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        //공급사 확인 , 추후 공급사 추가 예정 고려
        OAuth2ResponseSocial responseSocial;

        if ("google".equals(registrationId)) {
            responseSocial = new GoogleOAuth2Response((oAuth2User.getAttributes()));
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
        }
        // DB에서 사용자 찾기 (없으면 새로 저장)
        User repositoryUser = userRepository.findByEmail(responseSocial.getEmail())
                .orElseGet(() -> userRepository.save(User.toEntity(
                        UserDto.of(
                                responseSocial.getEmail(),
                                "",
                                responseSocial.getName(),
                                Role.USER,
                                responseSocial.getProvider(),
                                responseSocial.getProviderId(),
                                UserStatus.ACTIVE,
                                null
                        )
                )));

        UserDto userDto = UserDto.toDto(repositoryUser);
        log.info(" ✅  저장된 사용자: {}", userDto);

        // ✅ OAuth2User -> MvPrincipalDetails 변환
        MvPrincipalDetails mvPrincipalDetails;
        if (oAuth2User instanceof OidcUser oidcUser) {
            log.info(" ✈️✈️✈️ OidcUser ");
            mvPrincipalDetails = MvPrincipalDetails.of(UserDto.toDto(repositoryUser), oidcUser, LoginType.OIDC);
        } else {
            log.info(" ⛴️⛴️⛴️ OAuth2User ");
            mvPrincipalDetails = MvPrincipalDetails.of(UserDto.toDto(repositoryUser), oAuth2User.getAttributes(),LoginType.OAUTH2);
        }
        return mvPrincipalDetails;
    }

}


