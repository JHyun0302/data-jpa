package study.datajpa.repository;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOnly {
    /**
     * - 단점: DB에서 엔티티 필드 다 조회해온 다음에 계산 -> JPQL Select절 최적화 안됨
     *
     * @Value 있으면: Open Projections
     * @Value 없으면: Close Projections
     */
    @Value("#{target.username + ' ' + target.age + ' ' + target.team.name}")
    String getUsername();
}
