package study.datajpa.repository.custom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Custom 기능에 다 몰아넣지 말고
 * - 핵심 biz 기능과 아닌 것 분리
 * - life cycle에 따라 repository 쪼개기
 * - ex) 화면 수정하는데 필요한 복잡한 동적 쿼리 = MemberQueryRepository (class 형태로 생성해서 직접 JPQL 쓰기)
 * - ex) 간단한 조회 or 핵심 biz 기능 쿼리 = MemberRepository (Spring Data JPA) + MemberRepositoryImpl (Querydsl)
 */
@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {
    private final EntityManager em;

    List<Member> findAllMembers() {
        return em.createQuery("select m from Member m")
                .getResultList();
    }
}
