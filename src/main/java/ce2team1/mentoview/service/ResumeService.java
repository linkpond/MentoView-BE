package ce2team1.mentoview.service;

import ce2team1.mentoview.controller.dto.response.InterviewResp;
import ce2team1.mentoview.controller.dto.response.ResumeResp;
import ce2team1.mentoview.entity.Resume;
import ce2team1.mentoview.entity.User;
import ce2team1.mentoview.exception.ResumeException;
import ce2team1.mentoview.repository.InterviewRepository;
import ce2team1.mentoview.repository.ResumeRepository;
import ce2team1.mentoview.repository.UserRepository;
import ce2team1.mentoview.service.dto.InterviewDto;
import ce2team1.mentoview.service.dto.ResumeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final AwsS3Service awsS3Service;
    private static final String RESUME_DIR = "resumes";
    private static final int RESUME_LIMIT = 5;

    // userId 값별로 resume list 출력
    // resumeId 값 별로 interview list 출력
    public List<ResumeResp> getResumesByUserId(Long userId) {

        // Validate user existence
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Fetch resumes and map to ResumeResp with interviews
        List<ResumeResp> resumes = resumeRepository.findByUserUserId(userId).stream()
                .map(ResumeDto::from)
                .map(ResumeResp::from)
                .toList();

        for (ResumeResp resume : resumes) {
            List<InterviewResp> interviewList = interviewRepository.findAllByResumeResumeId(resume.getResumeId())
                    .stream()
                    .map(InterviewDto::toDto)
                    .map(InterviewResp::from)
                    .toList();

            resume.updateInterviewList(interviewList);
        }

        return resumes;
    }

    // s3 bucket으로 resume Dir에서 userid Dir 별로 resume 파일 업로드
    public ResumeDto createResume(MultipartFile file, Long userId) {

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Validate user
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Check resume limit
        long activeResumeCount = resumeRepository.countAllByUserUserIdAndDeleteStatusIsFalse(userId);
        if (activeResumeCount >= RESUME_LIMIT) {
            throw new IllegalStateException("Cannot create more than " + RESUME_LIMIT + " resumes");
        }

        // Upload to S3
        String key = RESUME_DIR + "/" + userId;
        try {
            key = awsS3Service.uploadS3(file, key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }

        // Create and save resume
        Resume resume = Resume.of(file.getOriginalFilename(), key, user);
        resume = resumeRepository.save(resume);
        return ResumeDto.from(resume);
    }

    // s3 bucket에서 resume 삭제
    // db 정보는 title명만 변경해주는 softDelete 방식
    public void deleteResume(Long resumeId, Long userId) {

        // Validate user
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeException("Resume not found", HttpStatus.NOT_FOUND));

        // Check ownership
        if (!resume.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this resume");
        }

        // Delete from S3 and soft delete in DB
        awsS3Service.deleteS3Object(resume.getS3Key());
        resume.softDelete();
        resumeRepository.save(resume);
    }

    public void hardDeleteResume(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Resume> resumes = resumeRepository.findAllByUserId(userId);
        if (resumes.isEmpty()) {
            log.info("[S3 삭제] 유저 ID {}: 삭제할 이력서가 없습니다.", userId);
            return;
        }
        for (Resume resume : resumes) {
            if (resume.getS3Key()!=null) {
                log.info("[S3 삭제] Resume ID: {}, S3 Key: {}", resume.getResumeId(), resume.getS3Key());
                awsS3Service.deleteS3Object(resume.getS3Key());
            }
        }
        log.info("[S3 삭제 완료] 유저 ID {}: 모든 이력서 삭제 완료", userId);
    }
}