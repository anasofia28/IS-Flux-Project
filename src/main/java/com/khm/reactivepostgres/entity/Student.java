package com.khm.reactivepostgres.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;

public class Student {

    @Id
    Long id;
    String name;
    String birthdate;
    int credits;
    int grade;

    public Student() {}

    public Student(Long id, String name, String birthdate, int credits, int grade){
        this.id = id;
        this.name = name;
        this.birthdate = birthdate;
        this.credits = credits;
        this.grade = grade;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthdate() {
        return this.birthdate;
    }

    public void setBirthDate(String birthdate) {
        this.birthdate = birthdate;
    }

    public int getCredits() {
        return this.credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getGrade() {
        return this.grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public Student( String name, String birthdate, int credits, int grade){
        this.name = name;
        this.birthdate = birthdate;
        this.credits = credits;
        this.grade = grade;
    }

}
