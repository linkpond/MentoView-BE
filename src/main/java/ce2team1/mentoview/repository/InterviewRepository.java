package ce2team1.mentoview.repository;

import ce2team1.mentoview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findAllByResumeResumeId(Long resumeId);

    @Query("select i from Interview i " +
            "where i.resume.resumeId in :resumeIds")
    List<Interview> findAllByResumeId(@Param("resumeIds") Collection<Long> resumeIds);

}