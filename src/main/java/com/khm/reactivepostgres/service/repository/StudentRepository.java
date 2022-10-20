package com.khm.reactivepostgres.service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.khm.reactivepostgres.entity.Student;

public interface StudentRepository extends ReactiveCrudRepository<Student, Long> {
    
}
