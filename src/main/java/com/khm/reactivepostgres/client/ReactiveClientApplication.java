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

            Flux<Student> stream = client
                .get()  
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class);


            Mono<Integer> sum = stream
                    .map( s -> s.getGrade())
                    .reduce(0,(x1, x2) -> x1 + x2);

            Mono<Long> count = stream
                    .count(); 
            
            Flux.zip(sum, count)
                .map(x-> calculateAverage(x.getT1(),x.getT2()))
                .doOnNext(cr-> System.out.println("Average grades: " + String.valueOf(cr)))
                .blockLast();

            
                //a.mean();

            System.out.println("---Student who finished grades average---");
        
            Flux<Student> stream2 = client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .filter(gr -> gr.getCredits() == 180);
            
            Mono<Integer> sum2 =  stream2
                        .map( s -> s.getGrade())
                        .reduce(0,(x1, x2) -> x1 + x2);
                    
            Mono<Long> count2 = stream2
                        .count();
            
            Float mean2 = Flux.zip(sum2, count2)
                .map(x-> calculateAverage(x.getT1(),x.getT2()))
                .blockLast();
            
            System.out.println("Average grade for finished students: " + String.valueOf(mean2));

            Long fluxSize = count2.block();

            stream2
                .map(x -> calculateStandardDeviation(x.getGrade(), mean2))
                .reduce((t, u) -> Double.sum(t, u))
                .map(x ->  Math.sqrt(x/fluxSize))
                .doOnNext(cr-> System.out.println("Standard deviation: " + String.valueOf(cr)))
                .block();        


        };
    }

    public float calculateAverage(int sum, long size){
        return (float)sum/size;
    }

    public double calculateStandardDeviation(int grade, float mean){
        return Math.pow(grade - mean, 2);

    }
}
