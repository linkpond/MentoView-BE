package ce2team1.mentoview.service;

import ce2team1.mentoview.entity.User;
import ce2team1.mentoview.entity.atrribute.Role;
import ce2team1.mentoview.entity.atrribute.UserStatus;
import ce2team1.mentoview.exception.ServiceException;
import ce2team1.mentoview.repository.SubscriptionRepository;
import ce2team1.mentoview.repository.UserRepository;
import ce2team1.mentoview.service.dto.UserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;


    public UserDto createUser(UserDto userDto) {

        Optional<User> findByEmail = userRepository.findByEmail(userDto.getEmail());

        if (findByEmail.isPresent() && findByEmail.get().getSocialProvider() != null) {
            throw new IllegalArgumentException("Social provider already exists");
        }

        User userBuilder = User.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .name(userDto.getName())
                .role(Role.USER)
                .status(UserStatus.ACTIVE).build();

        userRepository.save(userBuilder);
        return UserDto.toDto(userBuilder);

    }

    public UserDto accessMyPage(Long userId, String password) {

        User findUser = userRepository.findById(userId).orElseThrow(() -> new ServiceException("User not found"));


        if(findUser.getPassword() == null || findUser.getPassword().isEmpty()) {
            throw new ServiceException("Password is empty");
        }

        if (!passwordEncoder.matches(password, findUser.getPassword())) {
            throw new ServiceException("Password does not match");
        }
        return UserDto.toDto(findUser);

    }


    public void changePassword(Long userId, String beforePassword, String afterPassword) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ServiceException("User not found"));
        UserDto userDto = UserDto.toDto(user);

        if (!passwordEncoder.matches(beforePassword, userDto.getPassword())) {
            throw new ServiceException("Password does not match");
        }
        UserDto changed = UserDto.toDto(user).toBuilder()
                .password(passwordEncoder.encode(afterPassword))
                .build();
        userRepository.save(User.toEntity(changed));

    }

/*    // OAuth
    public User updateUser(User user, OAuth2ResponseSocial responseSocial) {
        user.updateSocialInfo(responseSocial.getProviderId());
        return userRepository.save(user);
    }
    // OAuth
    public User createUser(OAuth2ResponseSocial responseSocial) {
        return User.builder()
                .email(responseSocial.getEmail())
                .name(responseSocial.getName())
                .role(Role.USER)
                .socialProvider(responseSocial.getProvider())
                .providerId(responseSocial.getProviderId())
                .status(UserStatus.ACTIVE)
                .build();
    }*/

    @Transactional
    public void setBillingKey(Long uId, String billingKey) {
        User user = userRepository.findById(uId).orElseThrow();
        userRepository.save(user.updateBillingKey(billingKey));

    }

    public String getBillingKey(Long uId) {
        User user = userRepository.findById(uId).orElseThrow();
        return user.getBillingKey();
    }

    public UserDto findByEmail(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ServiceException("User not found"));
        return UserDto.toDto(user);
    }
}