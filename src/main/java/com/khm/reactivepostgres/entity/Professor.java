package com.khm.reactivepostgres.entity;

import org.springframework.data.annotation.Id;

public class Professor {

    @Id
    private Long id;
    private String name;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Professor(String name){
        this.name = name;
    }

    public Professor(){}
    
}
