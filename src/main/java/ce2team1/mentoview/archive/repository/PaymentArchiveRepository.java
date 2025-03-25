package ce2team1.mentoview.archive.repository;

import ce2team1.mentoview.archive.entity.PaymentArchive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentArchiveRepository extends JpaRepository<PaymentArchive, Long> {
}