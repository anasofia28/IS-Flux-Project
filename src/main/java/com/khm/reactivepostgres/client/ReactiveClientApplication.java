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
            //Perguntar ao stor se podemos apenas chamar 1 vez todos os clientes
            client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .doOnNext(cr -> System.out.println("Name: " + cr.getName() + " BirthDate: " + cr.getBirthdate().toString()))
                .blockLast();


            System.out.println("---Number Students---");
            client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .count()
                .doOnNext(cr -> System.out.println("Count " + cr))
                .block(); 
                
            
 

            System.out.println("---Students still active ---");
            
            client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .filter(gr -> gr.getCredits() < 180)
                .doOnNext(cr -> System.out.println("Name: " + cr.getName() + " Credits:" + cr.getCredits()))
                .blockLast();

            System.out.println("---Courses completed---");
            client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .doOnNext(cr -> System.out.println("Name: " + cr.getName() + " Number Courses:" + cr.getCredits()/6))
                .blockLast();

            System.out.println("---Students in last year---");
            client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .filter(gr -> gr.getCredits() < 180 && gr.getCredits() >= 120)
                .sort((s1,s2) -> {return s2.getCredits()-s1.getCredits();})
                .doOnNext(cr -> System.out.println("Name: " + cr.getName() + " Credits:" + cr.getCredits()))
                .blockLast();

            

            System.out.println("---Student grades average---");

            client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .collectList()
                .map( (x) -> {
                    float sum = 0f;
                    for(int i = 0; i < x.size(); i++){
                        sum += x.get(i).getGrade();
                    }
                    return sum/x.size();})
                .doOnNext(System.out::println)
                .block();
            
                //a.mean();

            System.out.println("---Student who finished grades average---");
            
            
            client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .filter(gr -> gr.getCredits() == 180)
                .collectList()
                .map( (x) -> {
                    float mean = 0;
                    for(int i = 0; i < x.size(); i++){
                        mean += x.get(i).getGrade();
                    }
                    
                    mean = mean/x.size();
                    System.out.println(mean);
                    
                    float aux = 0;
                    for(int i = 0; i < x.size(); i++){
                        aux += Math.pow(x.get(i).getGrade() - mean,2);
                    }
                    return Math.sqrt(aux/x.size());})
                .doOnNext(System.out::println)
                .block();

            /*System.out.println("---Student who finished grades standard deviation---");
            client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .filter(gr -> gr.getGrade() == 180)
                .collectList()
                .map( (x) -> {
                    float sum = 0f;
                    for(int i = 0; i < x.size(); i++){
                        sum += x.get(i).getGrade();
                    }
                    return sum/x.size();})
                .doOnNext(System.out::println)
                .block();*/
        };
    }
}
