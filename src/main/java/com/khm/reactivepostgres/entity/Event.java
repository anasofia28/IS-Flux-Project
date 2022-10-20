package com.khm.reactivepostgres.entity;

import java.util.Date;

import lombok.Data;


@Data
public class Event{
    public long id;
    public Date when;

    public Event(){
    }

    public Event(long id, Date when){
        this.id = id;
        this.when = when;
    }


    
}