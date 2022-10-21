package com.khm.reactivepostgres.service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.relational.core.sql.render.SqlRenderer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khm.reactivepostgres.entity.Event;
import com.khm.reactivepostgres.entity.Student;
import com.khm.reactivepostgres.service.repository.ProfessorRepository;
import com.khm.reactivepostgres.service.repository.StudentRepository;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.core.publisher.Flux;
import io.r2dbc.spi.ConnectionFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;




@SpringBootApplication
@EnableR2dbcAuditing
@RestController
public class ReactiveServiceApplication{

    StudentRepository sr;
    ProfessorRepository pr;
  
    @GetMapping("/student/getStudents")
    Flux<Student> eventById(){
      return sr.findAll();
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE, value = "/events")
    Flux<Event> events(){
        Flux<Event> eventFlux = Flux.fromStream(Stream.generate(()-> new Event(System.currentTimeMillis(), new Date())));
        Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(1));
        return Flux.zip(eventFlux, durationFlux).map(Tuple2::getT1);
    }


    @Bean
    ConnectionFactoryInitializer initializer(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
      ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
      initializer.setConnectionFactory(connectionFactory);
      ResourceDatabasePopulator resource =
          new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
      initializer.setDatabasePopulator(resource);
      return initializer;
    }

    public static void main(String[] args){
        SpringApplication.run(ReactiveServiceApplication.class, args);
    }


    @Bean
  public CommandLineRunner run(StudentRepository studentRepository, ProfessorRepository professorRepository) {

    
    return (args) -> {
      sr = studentRepository;
      pr = professorRepository;
      studentRepository.deleteAll().subscribe();
        // save a few customers
      studentRepository.saveAll(Arrays.asList(new Student( "Rodas","09-06-2001", 70, 3.2f),
          new Student( "Edgar", "27-02-2001", 120, 3.2f),
          new Student("Alexy", "23-10-1997", 180, 3.2f),
          new Student( "Tatiana", "05-05-2001", 30, 3.2f),
          new Student( "Sofia","30-12-2001", 140, 3.2f)))
          .blockLast(Duration.ofSeconds(10));

      //studentRepository.save(new Student("WQRWQERWQRWQR","16-10-2022", 0, 3.2f)).subscribe();



      
      // fetch all customers
      /*log.info("Customers found with findAll():");
      log.info("-------------------------------");
      studentRepository.findAll().doOnNext(customer -> {
        log.info(customer.toString());
      }).blockLast(Duration.ofSeconds(10));

      log.info("");

            // fetch an individual customer by ID
        studentRepository.findById(1L).doOnNext(customer -> {
        log.info("Customer found with findById(1L):");
        log.info("--------------------------------");
        log.info(customer.toString());
        log.info("");
      }).block(Duration.ofSeconds(10));


      // fetch customers by last name
      log.info("Customer found with findByLastName('Almeida'):");
      log.info("--------------------------------------------");
      studentRepository.findByLastName("Almeida").doOnNext(bauer -> {
        log.info(bauer.toString());
        }).blockLast(Duration.ofSeconds(10));;
        log.info("");*/
    };
}
}
