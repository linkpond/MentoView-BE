package ce2team1.mentoview.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for {@link ce2team1.mentoview.entity.User}
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPasswordModify {

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String beforePassword;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, max =15, message = "비밀번호 최소 8자 이상, 최대 15자 이하로 입력해주세요")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[.@$!%*?&\\^~+=()\\[\\]#\\-])[A-Za-z\\d.@$!%*?&\\^~+=()\\[\\]#\\-]{8,15}$",
            message = "비밀번호는 최소 8자 이상 15자 이하이며, 숫자와 특수문자를 포함해야 합니다.")
    private String afterPassword;

    @NotBlank(message = "비밀번호 확인을 입력해주세요")
    private String afterPasswordCheck;

}
