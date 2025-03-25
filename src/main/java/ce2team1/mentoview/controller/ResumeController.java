package ce2team1.mentoview.controller;

import ce2team1.mentoview.controller.dto.response.ResumeResp;
import ce2team1.mentoview.security.dto.MvPrincipalDetails;
import ce2team1.mentoview.service.ResumeService;
import ce2team1.mentoview.service.dto.ResumeDto;
import ce2team1.mentoview.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping
    public ResponseEntity<List<ResumeResp>> getFullRes(@AuthenticationPrincipal MvPrincipalDetails mvPrincipalDetails ) {

        System.out.println("principal - " + mvPrincipalDetails);
        UserDto dto = mvPrincipalDetails.getUserDto();
        System.out.println("UserDto - " + dto);

        Long userId = mvPrincipalDetails.getUserId();
       // Long userId = 1L;

        List<ResumeResp> resumes = resumeService.getResumesByUserId(userId);
        return ResponseEntity.ok(resumes);
    }

    @PostMapping
    public ResponseEntity<ResumeResp> createRes(@RequestParam("file") MultipartFile file,
                                                @AuthenticationPrincipal MvPrincipalDetails mvPrincipalDetails) {

        Long userId = mvPrincipalDetails.getUserId();
        // Long userId = 1L;

        ResumeDto resumeDto = resumeService.createResume(file, userId);
        ResumeResp response = ResumeResp.from(resumeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteRes(@PathVariable Long resumeId,
                                          @AuthenticationPrincipal MvPrincipalDetails mvPrincipalDetails) {

        Long userId = mvPrincipalDetails.getUserId();
        // Long userId = 1L;

        resumeService.deleteResume(resumeId, userId);
        return ResponseEntity.ok().build();
    }
}
