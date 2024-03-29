package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;
import study.datajpa.repository.custom.MemberQueryRepository;
import study.datajpa.repository.projections.NestedClosedProjections;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    private final MemberQueryRepository memberQueryRepository;

    @Autowired
    public MemberRepositoryTest(MemberQueryRepository memberQueryRepository) {
        this.memberQueryRepository = memberQueryRepository;
    }

    @PersistenceContext
    EntityManager em;


/*    @BeforeEach
    public void init() {
        memberRepository.deleteAllInBatch();
    }*/

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();
//        memberRepository.findById(savedMember.getId()).orElseThrow(() -> new NoSuchElementException("No value present")); //정석
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

        assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        findMember1.setUsername("member!!!!!!!!!!");
        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    /**
     * Spring Data JPA 간단한 Crud
     * - find...By, count...By, exists...By, delete...By, find...DistinctBy, findFirst3
     */
    @Test
    public void findHelloBy() { //전체 조회 test
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> helloBy = memberRepository.findHelloBy();
        List<Member> top3HelloBy = memberRepository.findTop3HelloBy(); //query: limit 3
        Long count = memberRepository.countHelloBy(); //개수
        List<Member> memberDistinctBy = memberRepository.findMemberDistinctBy(); //중복 제거
        boolean exist = memberRepository.existsMemberBy();
        System.out.println("exist = " + exist);
        System.out.println("count = " + count);

        long delete = memberRepository.deleteByAge(10);
        System.out.println("delete = " + delete);
    }

    /**
     * NamedQuery
     */
    @Test
    public void testNamedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    /**
     * @Query(jpql)
     */
    @Test
    public void testQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    /**
     * @Query(jpql): 단순히 값 하나를 조회
     */
    @Test
    public void findUsernameList() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    /**
     * new operation 문법...return Dto
     */
    @Test
    public void findUsernameDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    /**
     * 컬렉션 파라미터 바인딩: Collection 타입으로 in절 지원
     */
    @Test
    public void findByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    /**
     * 반환 타입(단건, 컬렉션, Optional)
     * - 단건 조회: 결과 없음 - null
     * - 컬렉션: 결과 없음 - 빈 컬렉션
     * 2건 이상 - NoUniqueResultException 발생
     */
    @Test
    public void returnType() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> aaa = memberRepository.findListByUsername("AAA");
        Member aaa1 = memberRepository.findMemberByUsername("AAA");
        Optional<Member> aaa2 = memberRepository.findOptionalByUsername("AAA");

        List<Member> asdfasf = memberRepository.findListByUsername("asdfasf"); // 데이터 없는 경우
        assertThat(asdfasf.size()).isEqualTo(0);
    }

    /**
     * page로 찾으면 "select count" 쿼리도 같이 나감!
     */
    @Test
    public void paging() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        //0페이지에서 3개 가져와. 사용자 이름 내림차순 정렬
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
//        Slice<Member> slice = memberRepository.findByAge(age, pageRequest);
//        Page<Member> page = memberRepository.findTop3ByAge(age); // 3건만 조회

        /**
         * 페이지 유지하면서 엔티티 -> DTO 변환 (API 반환)
         */
        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null)); //API 반환 가능

        //then
        List<Member> content = page.getContent(); //조회된 데이터: 3개
        long totalElements = page.getTotalElements();
        for (Member member : content) {
            System.out.println("member = " + member);
        }

        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(105); //전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(35); //전체 페이지 번호
        assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
    }

    @Test
    public void bulkUpdate() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);
        em.flush();
//        em.clear(); //DB는 41살, but 영속성 컨텍스트 데이터: 40... 즉 영속성 컨텍스트 data 날려버리기 or @Modifying(clearAutomatically = true) 속성 사용

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);

        //then
        assertThat(resultCount).isEqualTo(83);
    }

    /**
     * @EntityGraph member -> team 지연로딩 (N + 1)
     */
    @Test
    public void findMemberLazy() {
        //given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when
//        List<Member> members = memberRepository.findAll(); //member 모두 찾기: N+1 문제 발생! (1)
//        List<Member> members = memberRepository.findMemberFetchJoin(); // 페치 조인으로 N+1 해결 ---> 직접 JPQL 써야하는 단점
//        List<Member> members = memberRepository.findAll(); //Entity Graph 써서 N+1 해결
        List<Member> members = memberRepository.findEntityGraphByUsername("member1"); //메서드 이름으로 Entity Graph 사용

        //then
        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass()); //Lazy 설정이라 가짜 프록시를 초기화해서 찍힘! (N) - 해결
            System.out.println("member.team = " + member.getTeam().getName()); //team의 진짜 이름 얻기 (쿼리 나감) (N) - 해결
        }
    }

    /**
     * JPA Hint & Lock
     */
    @Test
    public void queryHint() {
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        //when
//        Member findMember = memberRepository.findById(member1.getId()).get(); //변경 감지때문에 원복 data 저장 -> 메모리 낭비
        Member findMember = memberRepository.findReadOnlyByUsername("member1"); // @QueryHint - readOnly: true
        findMember.setUsername("member2");

        em.flush();
    }

    @Test
    public void lock() {
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        //when
        List<Member> result = memberRepository.findLockByUsername("member1");
    }

    /**
     * 사용자 정의 리포지토리 구현
     */
    @Test
    public void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }

    /**
     * 나머지 기능 - 명세(Specification)
     * - 실무에서 안 씀(JPA Criteria 활용해야 해서...)
     */
    @Test
    public void specBasic() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec);

        assertThat(result.size()).isEqualTo(1);
    }

    /**
     * Query By Example
     * - 동적 쿼리 생성 가능. 도메인 객체를 그대로 사용.
     * - 단점: Outer 조인 안됨. Inner Join만 지원 => QueryDsl 쓰자!!
     * - Probe: 필드에 데이터가 있는 실제 도메인 객체
     * - ExampleMatcher: 특정 필드를 일치시키는 상세한 정보 제공, 재사용 가능
     * - Example: Probe & ExampleMatcher 구성, 쿼리 생성하는데 사용
     */
    @Test
    public void queryByExample() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        //Probe 생성: 실제 도메인 객체
        Member member = new Member("m1");
        Team team = new Team("teamA");
        member.setTeam(team);

        //ExampleMatcher : age 프로퍼티는 무시
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");

        //Example
        Example<Member> example = Example.of(member, matcher);

        List<Member> result = memberRepository.findAll(example);

        //then
        assertThat(result.get(0).getUsername()).isEqualTo("m1");
    }

    /**
     * Projections: select 절에 들어갈 data (객체 전체를 조회하는게 아니라 name 같이 값 1개만 찍어서 가져올 때)
     */

    @Test
    public void projections() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        //인터페이스 기반
//        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1");
//        for (UsernameOnly usernameOnly : result) {
//            System.out.println("usernameOnly = " + usernameOnly);
//        }
        //클래스 기반
//        List<UsernameOnlyDto> result = memberRepository.findProjectionsByUsername("m1");
//        for (UsernameOnlyDto usernameOnly : result) {
//            System.out.println("usernameOnly = " + usernameOnly);
//        }
        //동적 projections(class 타입에 맞춰 동작)
//        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1", UsernameOnly.class);
//        for (UsernameOnly usernameOnly : result) {
//            System.out.println("usernameOnly = " + usernameOnly);
//        }
        //중첩 구조 처리
        List<NestedClosedProjections> result = memberRepository.findProjectionsByUsername("m1", NestedClosedProjections.class);
        for (NestedClosedProjections nestedClosedProjections : result) {
            String username = nestedClosedProjections.getUsername();
            System.out.println("username = " + username);
            String teamName = nestedClosedProjections.getTeam().getName();
            System.out.println("teamName = " + teamName);
        }
    }

    /**
     * 네이티브 쿼리 - 가급적 사용 X.
     */

    @Test
    public void nativeQuery() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        //네이티브 쿼리
//        Member result = memberRepository.findByNativeQuery("m1");
//        System.out.println("result = " + result);

        //네이티브 쿼리 + Projections
        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = result.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection = " + memberProjection.getUsername());
            System.out.println("memberProjection = " + memberProjection.getTeamName());
        }
    }
}