package ce2team1.mentoview.archive.entity;

import ce2team1.mentoview.entity.atrribute.Role;
import ce2team1.mentoview.entity.atrribute.UserStatus;
import ce2team1.mentoview.utils.jpaconverter.UserRoleConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
/*
* UserStatus가   DORMANT, DELETED 인 유저를 관리한다
*/

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access =  AccessLevel.PRIVATE)
public class UserArchive  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId; // 기존식별자

    @Column(unique = true, nullable = false)
    private String email; // 기존 ID

    @Convert(converter = UserRoleConverter.class)
    private Role role; // 기존역할

    @Enumerated(EnumType.STRING)
    private UserStatus status; // 상태 , DORMANT, DELETED만허용

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Column(columnDefinition = "TEXT")
    private String additionalData; // JSON 형식, 추가 데이터

    @Column(nullable = false)
    protected LocalDateTime archivedAt; // 아카이브된 시간

    public static UserArchive of(Long userId, String email, Role role, UserStatus status, LocalDateTime createdAt, String additionalData) {
        return UserArchive.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .status(status)
                .createdAt(createdAt)
                .expirationDate(LocalDateTime.now().plusYears(1))
                .additionalData(additionalData)
                .archivedAt(LocalDateTime.now())
                .build();
    }
}
