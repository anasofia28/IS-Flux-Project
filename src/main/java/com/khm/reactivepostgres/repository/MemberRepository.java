package com.khm.reactivepostgres.repository;

import com.khm.reactivepostgres.entity.Member;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MemberRepository extends ReactiveCrudRepository<Member, Long> {
  Mono<Member> findByName(String name);

  @Query("SELECT * FROM member WHERE last_name = :lastname")
  Flux<Member> findByLastName(String lastName);


  @Query("drop table if exists member")
  Mono<Void> dropTable();
}
