package com.example.study.repository;

import com.example.study.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {

    //JPA꺼 아닙니다!!! : queryDSL의 추상메서드
    List<Member> findByName(String name); //findByName : JPA 아님(상속(extends)도 안받음)

    List<Member> findUser(String nameParam, Integer ageParam);
}
