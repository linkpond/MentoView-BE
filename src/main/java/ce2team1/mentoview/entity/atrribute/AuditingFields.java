package ce2team1.mentoview.entity.atrribute;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class AuditingFields {
    @Column(name = "created_at", updatable = false, length = 20)
    @CreatedDate
    private LocalDateTime createdAt ;
    @CreatedBy
    @Column(name = "created_by",updatable = false, length = 50)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "modified_at", updatable = true, length = 50)
    private LocalDateTime modifiedAt;
    @LastModifiedBy
    @Column(name = "modified_by", updatable = true, length = 50)
    private String modifiedBy;
}
