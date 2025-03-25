package ce2team1.mentoview.security.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum LoginType {
    FORM("from"),
    OAUTH2("oauth2"),
    OIDC("oidc");


    private final String code;

    public static LoginType ByCode(String code) {
        return Arrays.stream(values()).filter(loginType
                -> loginType.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 LoginType: " + code));
    }

}
