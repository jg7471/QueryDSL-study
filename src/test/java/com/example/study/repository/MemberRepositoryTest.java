package com.example.study.repository;

import com.example.study.entity.Member;
import com.example.study.entity.QMember;
import com.example.study.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.study.entity.QMember.*;
import static com.example.study.entity.QTeam.team;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false) //false 안적어서 에러 떴엇음 @Rollback(false)는 테스트가 완료된 후 트랜잭션이 롤백되지 않고 커밋되도록 합니다.
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em; //JPA 관리 핵심 객체

    //QueryDSL로 쿼리문을 작성하기 위한 핵심 객체
    JPAQueryFactory factory;

    @BeforeEach //테스트 메서드 돌릴 때 마다 새로운 객체
    void settingObject() {
        factory = new JPAQueryFactory(em);
    }

    @Test
    void testInsertData() {

//        Team teamA = Team.builder()
//                .name("teamA")
//                .build();
//        Team teamB = Team.builder()
//                .name("teamB")
//                .build();

//        teamRepository.save(teamA);
//        teamRepository.save(teamB);
        Team teamA = teamRepository.findById(7L).orElseThrow();
        Team teamB = teamRepository.findById(8L).orElseThrow();

        Member member1 = Member.builder()
                .userName("member9")
                .age(50)
                .team(teamA)
                .build();
        Member member2 = Member.builder()
                .userName("member10")
                .age(50)
                .team(teamA)
                .build();
        Member member3 = Member.builder()
                .userName("member11")
                .age(30)
                .team(teamB)
                .build();
        Member member4 = Member.builder()
                .userName("member12")
                .age(80)
                .team(teamB)
                .build();

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
    }

    @Test
    @DisplayName("testJPA")
    void testJPA() {

        List<Member> members = memberRepository.findAll();
        members.forEach(System.out::println);
    }

    //生 JPA 방식(springJPA 아님)
    @Test
    @DisplayName("testJPQL")
    void testJPQL() {
        //원형
        //given
        String jpqlQuery = "SELECT m FROM Member m WHERE m.userName = :userName";

        //when
        Member foundMember = em.createQuery(jpqlQuery, Member.class)
                .setParameter("userName", "member2")
                .getSingleResult();

        //then
        assertEquals("teamA", foundMember.getTeam().getName());

        System.out.println("\n\n\n");
        System.out.println("foundMember = " + foundMember);
        System.out.println("foundMember.getTeam() = " + foundMember.getTeam());
        System.out.println("\n\n\n");

    }

    @Test
    @DisplayName("TestQueryDSL")
    void testQueryDSL() {
        // given
        //QueryDSL에서 사용할 Q클래스 객체르 받아온다. (클래스 내에 상수로 객체가 선언되어 있기 때문에 직접 생성할 필요가 없음)
        QMember m = member; //import로 member로만 출력 가능

        //String jpqlQuery = "SELECT m FROM member m WHERE m.userName = :userName";
        //위와 동일 하나 아래가 더 진보적(에러 부분을 더 명확하게 알 수 있다)

        // when
        Member foundMember = factory.select(m)
                .from(m)
                .where(m.userName.eq("member1"))
                .fetchOne();

        // then
        assertEquals(foundMember.getUserName(), "member1");

        //단숙 작업은 JPA 씀!!!
    }


    @Test
    @DisplayName("search")
    void search() {
        // given
        String searchName = "member2";
        int searchAge = 20;

        // when
        Member foundMember = factory.selectFrom(member)
                //.where(QMember.member.userName.eq(searchName), QMember.member.age.eq(searchAge))
                .where(member.userName.eq(searchName)
                        .and(member.age.eq(searchAge)))
                .fetchOne();

        // then
        assertEquals("teamA", foundMember.getTeam().getName());

    }

    /*
         JPAQueryFactory를 이용해서 쿼리문을 조립한 후 반환 인자를 결정합니다.
         - fetchOne(): 단일 건 조회. 여러 건 조회시 예외 발생.
         - fetchFirst(): 단일 건 조회. 여러 개가 조회돼도 첫 번째 값만 반환
         - fetch(): List 형태로 반환
         * JPQL이 제공하는 모든 검색 조건을 queryDsl에서도 사용 가능
         * eq : =
         * member.userName.eq("member1") // userName = 'member1'
         * member.userName.ne("member1") // userName != 'member1'
         * member.userName.eq("member1").not() // userName != 'member1'
         * member.userName.isNotNull() // 이름이 is not null
         * member.age.in(10, 20) // age in (10,20)
         * member.age.notIn(10, 20) // age not in (10,20)
         * member.age.between(10, 30) // age between 10, 30
         * member.age.goe(30) // age >= 30                       //goe 좌항 기준
         * member.age.gt(30) // age > 30
         * member.age.loe(30) // age <= 30
         * member.age.lt(30) // age < 30
         * member.userName.like("_김%") // userName LIKE '_김%'
         * member.userName.contains("김") // userName LIKE '%김%'
         * member.userName.startsWith("김") // userName LIKE '김%'
         * member.userName.endsWith("김") // userName LIKE '%김'
         */

    @Test
    @DisplayName("여러 결과 반환하기")
    void testFetchResult() {
        // fetch
        List<Member> fetch1 = factory.selectFrom(member).fetch();

        System.out.println("\n\n========== fetch ==========");
        fetch1.forEach(System.out::println);//soutc : m-> System.out.println(m)

        // fetchOne
        Member fetch2 = factory.selectFrom(member)
                .where(member.id.eq(3L))
                .fetchOne();

        System.out.println("\n\n========== fetch2 ==========");
        System.out.println("fetch2 = " + fetch2);

        // fetchFirst
        Member fetch3 = factory.selectFrom(member).fetchFirst();
        System.out.println("\n\n========== fetch3 ==========");
        System.out.println("fetch3 = " + fetch3);

    }

    @Test
    @DisplayName("QueryDSL custom 설정 확인")
        //MemberRepository
    void queryDslCustom() {
        // given
        String name = "member4";

        // when
        List<Member> result = memberRepository.findByName(name); //findByName : queryDSL


        // then
        assertEquals(1, result.size());
        assertEquals("teamB", result.get(0).getTeam().getName());
    }

    /////////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("회원 정렬 조회")
    void sort() {
        // given

        // when
        List<Member> result = factory.selectFrom(member)
                .where()//원하는 조건, 없으면 생략
                .orderBy(member.age.desc())
                .fetch();
        // then
        assertEquals(result.size(), 12);

        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");

    }

    @Test
    @DisplayName("queryDSL paging")
    void paging() {
        // given

        // when
        List<Member> result = factory.selectFrom(member)
                .orderBy(member.userName.desc())
                .offset(3) //LIMIT(3,3) : SQL처럼 쓸 시 2-1(페이지 수), 3(한 화면 보여줄 개수)
                .limit(3)
                .fetch();

        // then
        assertEquals(result.size(), 3);

        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("그룹 함수의 종류")
    void aggregation() {
        // given

        // when
        List<Tuple> result = factory.select(
                        member.count(),
                        member.age.sum(),  //SQL : sum(age)
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        //Tuple : QueryDSL에서 쿼리로 나온 결과 행들을 타입에 맞춰 담을 수 있게 제공되는 타입.
        Tuple tuple = result.get(0);

        // then
        assertEquals(tuple.get(member.count()), 12);
        assertEquals(tuple.get(member.age.max()), 80);
        assertEquals(tuple.get(member.age.min()), 10);

        System.out.println("\n\n\n");
        System.out.println("result = " + result);
        System.out.println("tuple = " + tuple);
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("GROUP BY, HAVING")
    void testGroupBy() {
        // given

        // when
        List<Tuple> result = factory.select(member.age, member.age.count())
                .from(member)
                .groupBy(member.age)
                .having(member.age.count().goe(2))
                .orderBy(member.age.asc())
                .fetch();

        // then
        result.forEach(System.out::println);
    }

    @Test
    @DisplayName("join 해보기")
    void join() {
        // given

        //Oracle DB의 경우 Oracle의 조인 문법도 사용이 가능하다
        //SELECT * FROM employees, departments WHERE ~~~~
        //SELECT().from(employees, departments).WHERE(~~~~)

        // when
        //List<Member> result = factory.selectFrom(member)
        List<Tuple> result = factory.select(member.userName, team.name)
                .from(member)
                //join(기준 Entity. 조인 대상 Entity, 별칭(QClass))
                .join(member.team, team) //조건절
                .where(team.name.eq("teamA"))
                .fetch();

        // then
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");

    }

    /*
    ex) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조회, 회원은 모두 조회되어야 한다.
    SQL : SELECT m.*, t.* FROM tbl_member m LEFT JOIN tbl_team t ON m.team_id = t.team_id WHERE t.name = 'teamA';
    JPQL : SELECT m, t FROM Member m LEFT JOIN m.team ON t,name = 'teamA'; //Member m이 가지고 있는 team이랑 붙이겠다(따로 Team 언급 안해도 ok)

    * */
    @Test
    @DisplayName("left outer join 테스트")
    void leftJoinTest() {
        //entity 기반
        //queryDSL : 굳이 쿼리 안돌려봐도, 작성 단계에서 빨간줄 그어줌(문법 틀리면 실행도 안됨) <> JPQL은 실행되어야 알게 됨
        // when
        List<Tuple> result = factory.select(member, team) //member, team 모든 것
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        // then
        System.out.println("\n\n\n");
        result.forEach(tuple -> System.out.println("tuple = " + tuple));
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("sub query 사용하기(나이가 가장 많은 회원 조회")
    void subQueryTest() {
        // given
        //같은 테이블에서 서브쿼리를 적용하려면 별도의 QClass의 객체를 생성해야 함
        QMember memberSub = new QMember("memberSub");

        // when
        List<Member> result = factory.selectFrom(member)
                .where(member.age.gt( //eq gt goe 등등
                        //나이가 가장 많은 사람을 조회하는 서브쿼리문이 들어가야 함
                        JPAExpressions //서브쿼리를 사용할 수 있게 해주는 클래스 : 서브쿼리는 별도의 Qclass 사용
                                .select(memberSub.age.avg())
                                .from(memberSub)
                        //age = (SELECT MAX(age) FROM tbl_member) : subquery
                ))
                .fetch();

        System.out.println("result = " + result);

        // then
    }

    @Test
    @DisplayName("나이가 평균 나이 이상인 회원을 조회")
    void subQueryGoe() {
        // given
        QMember m2 = new QMember("m2");
        // when
        List<Member> result = factory.selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                //JPAExpressions는 from 절을 제외하고 select와 where절에서 사용 가능
                                //JPQL도 마찬가지로 from절 서브쿼리 사용 불가
                                // -> Native SQL을 작성하던지, MyBatis or JdbcTemplate 이용, 따로따로 두 번 조회도 사용 可
                                .select(m2.age.avg())
                                .from(m2)
                )).fetch();

        // then
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("동적 sql 테스트")
    void dynamicQueryTest () {
        // given
        String name = "";
        Integer age = 10;
        // when
        List<Member> result = memberRepository.findUser(name, age);

        // then
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }

}