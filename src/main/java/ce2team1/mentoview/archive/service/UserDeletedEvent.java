package ce2team1.mentoview.archive.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Slf4j
@RequiredArgsConstructor
public class UserDeletedEvent {
    private final Long userId;


    public void start() {
        log.info("[{}, 데이터 삭제 시작 ]", userId);
    }
}
