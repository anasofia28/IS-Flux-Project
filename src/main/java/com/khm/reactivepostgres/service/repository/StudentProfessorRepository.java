package com.khm.reactivepostgres.service.repository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.khm.reactivepostgres.entity.StudentProfessor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StudentProfessorRepository extends ReactiveCrudRepository<StudentProfessor,Long> {
    @Query("select * from student_professor WHERE student_id = $1")
    Flux<StudentProfessor> findByStudentId(Long student_id);
    
    Flux<StudentProfessor> findByProfessorId(Long professor_id);

    @Query("delete from student_professor WHERE student_id = $1 and professor_id = $2")
    Mono<StudentProfessor> findByProfessorIdStudentId(Long professor_id, Long student_id);
}
