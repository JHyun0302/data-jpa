package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    /**
     * JPA 메서드 이름으로 쿼리 생성
     */
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    List<Member> findHelloBy(); //By 뒤에 없으면 전체 조회

    boolean existsMemberBy(); //반환타입: boolean

    Long countHelloBy(); //반환타입: Long

    List<Member> findMemberDistinctBy();

    long deleteByAge(int age);

    List<Member> findTop3HelloBy(); //위에서 3개

    /**
     * JPA NamedQuery: 실무에서 잘 안 씀
     *
     * @Query 생략해도 가능 -> 자동으로 JpaRepository<Member> + 메서드 명(findByUsername)
     */
//    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    /**
     * @Query, 리포지토리 메소드에 쿼리 정의하기: 실무에서 자주 사용!
     */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);
}
