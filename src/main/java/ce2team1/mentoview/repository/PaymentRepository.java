package ce2team1.mentoview.repository;

import ce2team1.mentoview.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
//    @Query("SELECT p FROM Payment p WHERE p.subscription.subId = :subId")
//    List<Payment> findAllBySubId(@Param("subId") Long subId);

    List<Payment> findBySubscription_SubId(Long subId);

    @Query("select p from Payment p join fetch p.subscription where p.subscription.subId in :subIds")
    List<Payment> findBySubIds(@Param("subIds") Collection<Long> subIds);

    @Transactional
    @Modifying
    @Query("delete from Payment p")
    int deleteFirstBy();
}