package study.datajpa.repository.InterfaceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Custom 기능에 다 몰아넣지 말고
 * - 핵심 biz 기능과 아닌 것 분리
 * - life cycle에 따라 뭘 변경
 * 고려해서 repository 쪼개기
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
