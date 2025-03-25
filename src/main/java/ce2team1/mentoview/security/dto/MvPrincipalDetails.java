package ce2team1.mentoview.security.dto;

import ce2team1.mentoview.entity.atrribute.UserStatus;
import ce2team1.mentoview.service.dto.UserDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

//Provider가 받아서 로그인 한다
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MvPrincipalDetails implements OAuth2User, UserDetails{

    private final UserDto userDto;
    private final Map<String, Object> attributes;
    private final OidcUser oidcUser; // OidcUser
    private final LoginType loginType;

    public static MvPrincipalDetails of(UserDto userDto) {
        return new MvPrincipalDetails(userDto, Collections.emptyMap(), null,null);
    }

    public static MvPrincipalDetails of(UserDto userDto, LoginType loginType) { //폼
        return new MvPrincipalDetails(userDto, Collections.emptyMap(), null,loginType);
    }
    public static MvPrincipalDetails of(UserDto userDto, Map<String, Object> attributes, LoginType loginType) {//OAuth2User전용
        return new MvPrincipalDetails(userDto, attributes, null, loginType);

    }
    public static MvPrincipalDetails of(UserDto userDto, OidcUser oidcUser, LoginType loginType) { // OIDC 구현체 전용
        return new MvPrincipalDetails(userDto, oidcUser.getAttributes(), oidcUser,loginType);
    }


    public UserDto getUserDto() {
        return userDto;
    }

    //검증 이메일로함
    @Override
    public String getName() {
        return userDto.getEmail();
    }

    public String getUsername() {
        return userDto.getEmail();
    }

    @Override
    public String getPassword() {
        if (userDto == null) {
            return "";
        }
        return userDto.getPassword() != null ? userDto.getPassword() : "";
    }

    public Long getUserId() { // Long id
        return userDto.getUserId();
    }

    public static UserDto toDto(MvPrincipalDetails mvPrincipalDetails) {
        return UserDto.builder()
                .userId(mvPrincipalDetails.getUserId())
                .email(mvPrincipalDetails.getName())
                .password(mvPrincipalDetails.getPassword() != null ? mvPrincipalDetails.getPassword() : "")
                .name(mvPrincipalDetails.getRealName())
                .build();
    }

    public String getRealName() {
        return userDto.getName();
    }


    @Override
    public boolean isEnabled() {
        return userDto.getStatus() != UserStatus.DELETED;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userDto.getRole().getCode()));
    }
    @Override
    public Map<String, Object> getAttributes() { // 받은 데이터값
        return this.attributes;
    }

}
