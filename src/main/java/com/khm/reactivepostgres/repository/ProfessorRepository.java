package com.khm.reactivepostgres.repository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.khm.reactivepostgres.entity.Professor;

public interface ProfessorRepository extends ReactiveCrudRepository<Professor, Long> {
    
}
