package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.custom.MemberRepositoryCustom;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, JpaSpecificationExecutor<Member> {
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
     * - @Query 생략해도 가능 -> 자동으로 JpaRepository<Member> + 메서드 명(findByUsername)
     */
//    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    /**
     * @Query, 리포지토리 메소드에 쿼리 정의하기: 실무에서 자주 사용!
     * - 장점: 'findByUsernameAndAgeGreaterThan'처럼 조건이 많아지면 메서드명이 너무 길어지는 단점 방지!
     */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    /**
     * @Query(jpql) - 값, DTO 조회하기
     */
    @Query("select m.username from Member m")
    List<String> findUsernameList();

    //new operation 문법 -> return Dto  ... QueryDsl 쓰기!!
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name)" +
            " from Member m join m.team t")
    List<MemberDto> findMemberDto();

    /**
     * 컬렉션 파라미터 바인딩: Collection 타입으로 in절 지원
     */
    @Query("select m from  Member  m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    /**
     * 반환 타입
     */
    List<Member> findListByUsername(String username); //컬렉션

    Member findMemberByUsername(String username); //단건

    Optional<Member> findOptionalByUsername(String username); //단건 Optional

    /**
     * 페이징과 정렬
     * count 쿼리 분리 가능 -> 성능 향상 (count하는데 join 필요 x)
     * <p>
     * Page : 추가 count 쿼리 결과를 포함하는 페이징
     * Slice : 추가 count 쿼리 없이 다음 페이지만 확인 가능 (내부적으로 limit + 1 조회)
     * List : 추가 count 쿼리 없이 결과만 반환
     */
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);

//    Slice<Member> findByAge(int age, Pageable pageable);


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
     * "EntityGraph" - JPQL없이 페치 조인 사용하기!
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
    @EntityGraph(attributePaths = {"team"})
//    @EntityGraph("Member.all")   //NamedEntityGraph: 잘 안씀
    List<Member> findEntityGraphByUsername(@Param("username") String name);

    /**
     * JPA Hint & Lock
     * .findById() 사용시 변경감지 때문에 원복 data 저장해놓음!(만약 100% 조회용으로 메서드 쓴다면... 메모리 낭비!!)
     * -> @QueryHints 를 통해 readOnly로 만들어서 스냅샷 생성 X
     */
    //readOnly: true - SnapShot 안 만듬 (성능 최적화)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

    /**
     * Projections: 엔티티 대신에 DTO를 편리하게 조회
     * 인터페이스 기반
     * - 전체 엔티티가 아닌 회원이름만 딱 조회
     */
//    List<UsernameOnly> findProjectionsByUsername(@Param("username") String username);

    /**
     * 클래스 기반
     * 파라미터 명(@Param)으로 매칭
     */
//    List<UsernameOnlyDto> findProjectionsByUsername(@Param("username") String username);

    /**
     * 동적 Projections : Generic Type 사용
     */
    <T> List<T> findProjectionsByUsername(String username, Class<T> type);

    /**
     * 네이티브 쿼리
     * 한계
     * 1. Sort 파라미터를 통한 정렬이 정상 동작하지 않을 수 있음.
     * 2. JPQL처럼 애플리케이션 로딩 시점에 문법 확인 불가
     * 3. 동적 쿼리 불가
     */
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);

    /**
     * 네이티브 쿼리 + Projections
     */
    @Query(value = "select m.member_id as id, m.username, t.name as teamName " +
            "from member m left join team t on m.team_id = t.team_id",
            countQuery = "select count(*) from member",
            nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);

}

