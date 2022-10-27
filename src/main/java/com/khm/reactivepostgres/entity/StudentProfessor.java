package com.khm.reactivepostgres.entity;

import org.springframework.data.annotation.Id;

public class StudentProfessor {

    @Id
    Long id;
    Long professor_id;
    Long student_id;

    public StudentProfessor(){}

    public StudentProfessor(Long professor_id, Long student_id){
        this.professor_id=professor_id;
        this.student_id=student_id;
    }
    
    public Long getProfessorId(){
        return this.professor_id;
    }

    public void setProfessorId(Long professor_id) {
        this.professor_id=professor_id;
    }

    public Long getStudentId(){
        return this.student_id;
    }

    public void setStudentId(Long student_id) {
        this.student_id=student_id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
