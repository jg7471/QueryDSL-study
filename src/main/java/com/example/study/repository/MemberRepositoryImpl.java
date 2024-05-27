package com.example.study.repository;

import com.example.study.entity.Member;
import com.example.study.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.example.study.entity.QMember.member;

//QueryDSL 사용시 必ず 이름 Impl 로 끝나야함(자동인식)
//원본 인터페이스 타입(MemberRepository)의 객체로도 사용이 가능

@RequiredArgsConstructor //private final JPAQueryFactory queryFactory;
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    //일반쿼리
    @Override
    public List<Member> findByName(String name) {
        return queryFactory
                .selectFrom(member)
                .where(member.userName.eq(name))
                .fetch();
    }

    @Override
    public List<Member> findUser(String nameParam, Integer ageParam) {
        return queryFactory
                .selectFrom(member)
                .where(nameEq(nameParam), ageEq(ageParam))
                .fetch();
    }

    // WHERE 절에 BooleanExpression 을 리턴하는 메서드를 직접 작성합니다.
    // nameEq는 전달받은 값이 없다면 null을 리턴하고, 그렇지 않을 경우 논리 표현식 결과를 리턴
    //WHERE 절에서는 null 값인 경우 조건을 건너 뜀(쿼리를 완성하지 않음)


    //동적 쿼리부분
    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) : null; //3항 연산식 표현 1
    }

    private BooleanExpression nameEq(String nameParam) {
        if (nameParam != null && nameParam.isEmpty()) {
            return member.userName.eq(nameParam); //풀어서 표현 2
        }
        return null;
    }

}
