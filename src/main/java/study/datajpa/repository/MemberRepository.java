package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
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

    /**
     * @Query, 값, DTO 조회하기
     */
    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name)" +
            " from Member m join m.team t")
    List<MemberDto> findMemberDto();

    /**
     * 컬렉션 파라미터 바인딩: Collection 타입으로 in절 지원
     */
    @Query("select m from  Member  m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    /**
     * 반환 타입
     */
    List<Member> findListByUsername(String username); //컬렉션

    Member findMemberByUsername(String username); //단건

    Optional<Member> findOptionalByUsername(String username); //단건 Optional

    /**
     * 페이징과 정렬
     * count 쿼리 분리 가능 -> 성능 향상
     */
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);

    /**
     * 벌크성 수정 쿼리: where 조건에 맞는 모든 쿼리 수정
     * - 벌크성 수정 쿼리는 영속성 컨텍스트 무시하고 실행되기 때문에 @Modifying or em.clear() 필수!
     */
    @Modifying(clearAutomatically = true) // ==.executeUpdate()
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    /**
     * JPQL 페치 조인
     */
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    /**
     * "EntityGraph"로 JPQL없이 페치 조인 사용하기!
     */
    //공통 메서드 오버라이드
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    //JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = "{team}")
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    //메서드 이름으로 쿼리에서 특히 편리하다.
//    @EntityGraph(attributePaths = {"team"})
    @EntityGraph("Member.all")
    //NamedEntityGraph
    List<Member> findEntityGraphByUsername(@Param("username") String name);

    /**
     * JPA Hint & Lock
     * .findById() 사용시 변경감지 때문에 원복 data 저장해놓음!(만약 100% 조회용으로 메서드 쓴다면... 메모리 낭비하는 꼴!)
     */
    //readOnly: true - SnapShot 안 만듬
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);
}

