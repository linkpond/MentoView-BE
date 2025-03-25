package ce2team1.mentoview.repository;

import ce2team1.mentoview.entity.Resume;
import ce2team1.mentoview.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserUserId(Long userId);
    long countByUserUserId(Long userId);
    long countAllByUserUserIdAndDeleteStatusIsFalse(Long userId);

    Long user(User user);

    @Query("select r from Resume r where r.user.userId = :userId")
    List<Resume> findAllByUserId(@Param("userId") Long userId);
}