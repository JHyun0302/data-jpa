package study.datajpa.repository;

import study.datajpa.entity.Member;

import java.util.List;

/**
 * 사용자 정의 인터페이스
 * - 간단한 내용은 스프링 data JPA 쓰고
 * - 복잡한 내용은 Custom해서 JPQL 사용
 */
public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
