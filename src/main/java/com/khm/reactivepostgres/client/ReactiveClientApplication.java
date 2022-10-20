package com.khm.reactivepostgres.client;

import java.util.Collections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.khm.reactivepostgres.entity.Event;
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

        System.out.println("WWEQRWERWERWERWERWERWER");
        return args -> {
            client
                .get()
                .uri("/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Event.class)
                .subscribe(System.out::println);
        };
    }
}
