package ce2team1.mentoview.archive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

@MappedSuperclass
public class ArchiveBase {

    @Column(nullable = false)
    protected LocalDateTime archivedAt; // 아카이브된 시간

    @PrePersist
    protected void prePersistArchivedAt(){
        this.archivedAt = LocalDateTime.now();
    }

}
