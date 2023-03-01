package study.datajpa.entity;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

/**
 * Auditing: 공통 관심사
 * - 엔티티 생성,변경 시점 추적
 */
@Getter
@MappedSuperclass //진짜 상속이 아닌 속성값(createDate, updateDate)만 테이블에서 같이 쓸 수 있게끔!
public class JpaBaseEntity {
    @Column(updatable = false)
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createDate = now;
        updateDate = now;
    }

    @PreUpdate
    public void preUpdate() {
        updateDate = LocalDateTime.now();
    }
}
