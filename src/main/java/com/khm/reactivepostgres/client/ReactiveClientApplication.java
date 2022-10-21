package com.khm.reactivepostgres.client;

import java.util.Collections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.khm.reactivepostgres.entity.Event;
import com.khm.reactivepostgres.entity.Student;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ReactiveClientApplication {
    
    @Bean
    WebClient client(){
        return WebClient.create("http://localhost:8080");
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(ReactiveClientApplication.class)
        .properties(Collections.singletonMap("server.port", "8081"))
        .run(args);    
    }


    @Bean
    CommandLineRunner demo(WebClient client){


        return args -> {

            //Get all students
            Flux<Student> a = client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class);

            
                
                a.doOnNext(cr -> System.out.println("Name: " + cr.getName() + " BirthDate: " + cr.getBirthdate().toString())).blockLast();
                a.blockFirst();

                Mono<Long> count = a.count(); 
                System.out.println("---Number Students---");
                count.doOnNext(cr -> System.out.println("Count: " + cr)).block();
 

                System.out.println("---Students still active ---");
                a.filter(gr -> gr.getCredits() < 180).doOnNext(cr -> System.out.println("Name: " + cr.getName() + " Credits:" + cr.getCredits())).blockLast();


                System.out.println("---Courses completed---");
                a.doOnNext(cr -> System.out.println("Name: " + cr.getName() + " Number Courses:" + cr.getCredits()/6)).blockLast();
                

                System.out.println("---Students in last year---");
                a.filter(gr -> gr.getCredits() < 180 && gr.getCredits() >= 120).sort((s1,s2) -> {return s2.getCredits()-s1.getCredits();}).doOnNext(cr -> System.out.println("Name: " + cr.getName() + " Credits:" + cr.getCredits())).blockLast();
            
        };
    }
}
