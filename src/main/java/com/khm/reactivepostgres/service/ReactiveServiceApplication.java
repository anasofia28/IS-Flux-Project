package com.khm.reactivepostgres.service;

import java.time.Duration;
import java.util.Arrays;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khm.reactivepostgres.entity.Professor;
import com.khm.reactivepostgres.entity.Student;
import com.khm.reactivepostgres.entity.StudentProfessor;
import com.khm.reactivepostgres.service.repository.ProfessorRepository;
import com.khm.reactivepostgres.service.repository.StudentProfessorRepository;
import com.khm.reactivepostgres.service.repository.StudentRepository;

import reactor.core.publisher.Mono;
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
public class ReactiveServiceApplication {

    StudentRepository sr;
    ProfessorRepository pr;
    StudentProfessorRepository spr;

    // ---------------------------------------------------------------------------------//
    // TEST URLS -----------------------------------------------------------------------//

    @GetMapping("/student/getStudents")
    Flux<Student> eventById() {
        return sr.findAll();
    }

    @GetMapping("/add/relationship/{id1}/{id2}")
    Mono<StudentProfessor> addRelationship(@PathVariable long id1, @PathVariable long id2) {
        StudentProfessor new_relation = new StudentProfessor(id1, id2);
        spr.save(new_relation).subscribe();
        return Mono.just(new_relation);
    }

    @GetMapping("/delete/relationship/{id1}")
    public String removeRelationship(@PathVariable long id1) {
        spr.deleteById(id1).subscribe();
        return "Relation " + String.valueOf(id1) + " removed successfully";
    }

    @GetMapping("get/studentProf/{id1}")
    Flux<Long> getStudentProfessors(@PathVariable long id1) {
        return spr.findByStudentId(id1)
                .map(x -> {
                    return x.getProfessorId();
                });
    }

    @GetMapping("get/professor/{id1}")
    Mono<Professor> getProfessor(@PathVariable long id1) {

        return pr.findById(id1);
    }

    @GetMapping("get/allProfessors")
    public Flux<Professor> getAllProfessors() {
        return pr.findAll();
    }

    // ---------------------------------------------------------------------------------//
    // AUTOMATIC CLIENT2 POPULATING DATABASE -------------------------------------------//

    @PostMapping("/add/student")
    String addStudent(@RequestBody Student s) {
        sr.save(s).subscribe();
        return "Student added!";
    }

    @PostMapping("/add/professor")
    String addProfessor(@RequestBody Professor p) {
        pr.save(p).subscribe();
        return "Professor added!";
    }

    @GetMapping("/add/relationships")
    Mono<StudentProfessor> addRelationship(@RequestBody StudentProfessor sp) {

        long studentId = sp.getStudentId();
        long professorId = sp.getProfessorId();

        StudentProfessor new_relation = new StudentProfessor(studentId, professorId);
        spr.save(new_relation).subscribe();
        return Mono.just(new_relation);
    }

    // ---------------------------------------------------------------------------------//

    @Bean
    ConnectionFactoryInitializer initializer(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        ResourceDatabasePopulator resource =
                new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        initializer.setDatabasePopulator(resource);
        return initializer;
    }

    public static void main(String[] args) {
        SpringApplication.run(ReactiveServiceApplication.class, args);
    }


    @Bean
    public CommandLineRunner run(StudentRepository studentRepository, ProfessorRepository professorRepository, StudentProfessorRepository studentProfessorRepository) {


        return (args) -> {

            sr = studentRepository;
            pr = professorRepository;
            spr = studentProfessorRepository;

            // save a few customers
            studentRepository.saveAll(Arrays.asList(new Student("Rodas", "09-06-2001", 70, 4),
                            new Student("Edgar", "27-02-2001", 120, 20),
                            new Student("Alexy", "23-11-1995", 180, 16),
                            new Student("Tatiana", "05-05-2001", 180, 15),
                            new Student("Sofia", "28-05-2001", 140, 16)))
                    .blockLast(Duration.ofSeconds(10));

            professorRepository.saveAll(Arrays.asList(new Professor("Filipe"),
                            new Professor("Andre"),
                            new Professor("Nuno")))
                    .blockLast(Duration.ofSeconds(10));


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
