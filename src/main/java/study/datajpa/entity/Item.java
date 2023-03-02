package study.datajpa.entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * 새로운 엔티티 구별하는 방법
 * - 문제: @GeneratedValue 안 쓰고 @Id 정하는 경우 값을 넘겨주므로 null or 0 이 아니게 됨!
 * -> "새로운 엔티티가 아니라고 판단!!!"
 * - sol: persistable 인터페이스 구현해서 판단 로직 변경 가능!
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item implements Persistable<String> {
    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;

    public Item(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() { //등록 날짜 == null 이면 새로운 객체!
        return createdDate == null;
    }
}
