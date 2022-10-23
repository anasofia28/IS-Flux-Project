package com.khm.reactivepostgres.service.repository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.khm.reactivepostgres.entity.Professor;

import reactor.core.publisher.Mono;

public interface ProfessorRepository extends ReactiveCrudRepository<Professor, Long> {

    Mono<Professor> findByName(String name);

}
