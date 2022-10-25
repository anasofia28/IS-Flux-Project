package com.khm.reactivepostgres.client;

import java.util.Collections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.khm.reactivepostgres.entity.Student;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class Client2{
    

    /*@Bean
    WebClient client2(){
        return WebClient.create("http://localhost:8080");
    }*/
    
    public static Student randomStudent(){
        return new Student( "Edgar Junior","09-06-2001", 70, 20);
    }

    public static void addStudents(WebClient client, int quantity){
        for( int i = 0; i < quantity; i++){
            
            Student s = randomStudent();
            client.post()   
            .uri("/add/student")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .body(Mono.just(s), Student.class);
        }


    }   

    public static void main(String[] args) {
        new SpringApplicationBuilder(ReactiveClientApplication.class)
        .properties(Collections.singletonMap("server.port", "8081"))
        .run(args);    
    }


    @Bean
    CommandLineRunner demo2(WebClient client){

        return args ->{

            //Sout with menu options
        //input ask value

    //Gerar estudantes
    //Gerar professores
    //Gerar as ligacoes
        //switch(input)


        //Example for add user
            addStudents(client, 1);
        };

    }

}