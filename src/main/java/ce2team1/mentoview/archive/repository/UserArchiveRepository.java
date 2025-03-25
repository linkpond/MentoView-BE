package ce2team1.mentoview.archive.repository;

import ce2team1.mentoview.archive.entity.UserArchive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserArchiveRepository extends JpaRepository<UserArchive, Long> {
}