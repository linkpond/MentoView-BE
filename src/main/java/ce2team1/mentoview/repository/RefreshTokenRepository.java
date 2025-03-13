package ce2team1.mentoview.repository;

import ce2team1.mentoview.security.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Boolean existsByRefreshToken(String refreshToken);

  @Transactional
  void deleteByRefreshToken(String refreshToken);

  Optional<RefreshToken> findByUserEmail(String email);

  @Query("select r from RefreshToken r where r.expirationDate < :threshold") // 임계값
  List<RefreshToken> findExpiringRefreshTokens(@Param("threshold") LocalDateTime threshold);


  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Transactional
  @Query("delete from RefreshToken r where r.maxExpirationDate < :now")
  int deleteMaxExpirationDate(@Param("now") LocalDateTime now);

}