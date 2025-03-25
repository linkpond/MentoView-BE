package ce2team1.mentoview.entity;


import ce2team1.mentoview.entity.atrribute.AuditingFields;
import ce2team1.mentoview.entity.atrribute.Role;
import ce2team1.mentoview.entity.atrribute.SocialProvider;
import ce2team1.mentoview.entity.atrribute.UserStatus;
import ce2team1.mentoview.service.dto.UserDto;
import ce2team1.mentoview.utils.jpaconverter.UserRoleConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class User extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email; // OAuth + 폼

    @Column(nullable = false)
    private String password;//폼전용

    private String name;

    @Convert(converter = UserRoleConverter.class)
    private Role role;

    @Column(nullable = true) @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;// OAuth

    @Column(nullable = true)
    private String providerId;// OAuth의 providerId

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private String billingKey;


    public static User of(String email, String password, String name, Role role, SocialProvider socialProvider, String providerId, boolean isForm, UserStatus status, String billingKey ) {
        return new User(
                null,
                email,
                password != null ? password : "",  // ✅ 소셜 로그인 사용자는 비밀번호 입력 필수
                name,
                role != null ? role : Role.USER,  // 기본값: USER
                socialProvider,
                providerId,
                status != null ? status : UserStatus.ACTIVE,  // 기본값: ACTIVE
                null);

    }
    public static User toEntity(UserDto userDto) {
        return User.builder()
                .userId(userDto.getUserId())
                .email(userDto.getEmail())
                .password(userDto.getPassword()  != null ? userDto.getPassword() : "")
                .name(userDto.getName())
                .role(userDto.getRole())
                .socialProvider(userDto.getSocialProvider())
                .providerId(userDto.getProviderId())
                .status(userDto.getStatus())
                .billingKey(userDto.getBillingKey())
                .build();

    }


    public User updateBillingKey(String billingKey) {
        return this.toBuilder()
                .billingKey(billingKey)
                .build();
    }



    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getUserId() != null && Objects.equals(getUserId(), user.getUserId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

}
