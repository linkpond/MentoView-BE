package ce2team1.mentoview.archive.service;

import ce2team1.mentoview.entity.User;
import ce2team1.mentoview.entity.atrribute.UserStatus;
import ce2team1.mentoview.repository.UserRepository;
import ce2team1.mentoview.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArchiveBatch {

    private final ArchiveService archiveService;
    private final UserRepository userRepository;
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void archiveDeletedUsers() {
        log.info("[유저 삭제 배치 시작] 탈퇴한 유저의 데이터를 아카이브");

        List<User> deletingUsers = userRepository.findAllByStatus(UserStatus.DELETED);
        if (!deletingUsers.isEmpty()) {
            log.info("[유저 삭제 배치 완료!] 탈퇴 유저 없음");
            return;
        }
        for (User deletingUser : deletingUsers) {
            Long userId = deletingUser.getUserId();
            log.info("[아카이브 진행 유저] userId = {}", userId);

            log.info("[아카이브 인터뷰 아카이브 시작]");
            archiveService.archiveUserInterviews(userId);
            log.info("[아카이브 인터뷰 아카이브 종료]");

            log.info("[아카이브 결제 아카이브 시작]");
            archiveService.archiveUserPayments(userId);
            log.info("[아카이브 결제 아카이브 종료]");

            log.info("[아카이브 유저  아카이브 시작]");
            archiveService.archiveUser(userId);
            log.info("[아카이브 유저  아카이브 종료]");

            log.info("[아카이브 배치 완료] 모든 유저 데이터 아카이브 작업 완료 ");

        }
    }


}
