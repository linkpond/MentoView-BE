package ce2team1.mentoview.archive;

import ce2team1.mentoview.archive.dto.InterviewData;
import ce2team1.mentoview.archive.dto.InterviewEntry;
import ce2team1.mentoview.archive.entity.*;
import ce2team1.mentoview.archive.repository.InterviewArchiveRepository;
import ce2team1.mentoview.archive.repository.PaymentArchiveRepository;
import ce2team1.mentoview.archive.repository.UserArchiveRepository;
import ce2team1.mentoview.archive.service.ArchiveService;
import ce2team1.mentoview.entity.*;
import ce2team1.mentoview.repository.*;
import ce2team1.mentoview.service.AwsS3Service;
import ce2team1.mentoview.utils.archive.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArchiveServiceTest {
    @Autowired
    private  InterviewRepository interviewRepository;
    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private PaymentArchiveRepository paymentArchiveRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private AwsS3Service awsS3Service;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserArchiveRepository userArchiveRepository ;
    @Autowired
    private InterviewArchiveRepository archiveRepository;
    @Autowired
    private ArchiveService archiveService;





    @Test
    public void archive() {
        Long id = 13L;

        InterviewArchive interviewArchive = archiveService.archiveUserInterviews(id);
        System.out.println("interviewArchive = " + interviewArchive);


    }

    @Test
    public void archive22() {
        Long userId = 13L;
        List<PaymentArchive> paymentArchives = archiveService.archiveUserPayments(userId);
        System.out.println("paymentArchives = " + paymentArchives.toString());


    }

    @Test
    public void archive32() {
        Long userId = 13L;

        archiveService.archiveUser(userId);

    }


}