package ce2team1.mentoview.entity.atrribute;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum UserStatus {
    ACTIVE, //("활성")
    SUSPENDED, //("정지")
    DORMANT,  // ("휴면")
    DELETED; //("탈퇴")

}
