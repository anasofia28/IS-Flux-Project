package com.khm.reactivepostgres.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

public class Member {

  @Id
  private Long id;

  private String name;
  private String lastName;

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

  public String getLastName() {
    return this.lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }


  public Member( String name, String lastName) {
    this.name = name;

    this.lastName = lastName;
  }
  
  public String toString() {
    return this.name + " " + this.lastName;
  }

}
