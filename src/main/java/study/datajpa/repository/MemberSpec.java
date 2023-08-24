package study.datajpa.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

/**
 * 명세: JPA Criteria 사용해서 Specification 구현
 * JPA Criteria -> JPQL 생성 -> 동적 쿼리 생성
 * - 실무 사용 X
 */
public class MemberSpec {
    //팀 이름이 검색 조건
    public static Specification<Member> teamName(final String teamName) {
        return (Specification<Member>) (root, query, builder) -> {
            if (StringUtils.isEmpty(teamName)) {
                return null;
            }

            Join<Member, Team> t = root.join("team", JoinType.INNER); //회원과 팀 조인
            return builder.equal(t.get("name"), teamName); //where 문
        };
    }

    public static Specification<Member> username(final String username) {
        return (Specification<Member>) (root, query, builder) ->
                builder.equal(root.get("username"), username);
    }
}

