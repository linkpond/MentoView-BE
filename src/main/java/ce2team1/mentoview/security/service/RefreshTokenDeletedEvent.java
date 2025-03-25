package ce2team1.mentoview.security.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class RefreshTokenDeletedEvent {

    private final LocalDateTime eventTime;

}
