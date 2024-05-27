package com.example.study.repository;

import com.example.study.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

//MemberRepository : JPA
//MemberRepositoryCustom : QueryDSL
//다중 상속 가능 : 통합

//MemberRepository로 2가지 기능 사용 가능(다중 상속)

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    //springJPA 방식
    //@Query("SELECT m FROM member m WHERE m.userName = :userName")
    //Optional<Member> findMember(@Param("userName") String userName);

}
