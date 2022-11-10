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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;


@SpringBootApplication
@EnableR2dbcAuditing
@RestController
public class ReactiveServiceApplication {
    static Logger logger = Logger.getLogger(ReactiveServiceApplication.class.getName());
    StudentRepository sr;
    ProfessorRepository pr;
    StudentProfessorRepository spr;

    // ---------------------------------------------------------------------------------//
    // TEST URLS -----------------------------------------------------------------------//

    @GetMapping("/student/getStudents")
    Flux<Student> eventById() {
        logger.info("All students were given");
        return sr.findAll();
    }

    @GetMapping("/add/relationship/{id1}/{id2}")
    Mono<StudentProfessor> addRelationship(@PathVariable long id1, @PathVariable long id2) {
        logger.info("Realtionship: " + String.valueOf(id1) + " " + String.valueOf(id2) + " added");
        StudentProfessor new_relation = new StudentProfessor(id1, id2);
        spr.save(new_relation).subscribe();
        return Mono.just(new_relation);
    }

    @GetMapping("/delete/relationship/{id1}")
    public String removeRelationship(@PathVariable long id1) {
        spr.deleteById(id1).subscribe();
        logger.info("Removed relationship: " + String.valueOf(id1));
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
        logger.info("Professors" + String.valueOf(id1) + " was given");
        return pr.findById(id1);
    }

    @GetMapping("get/allProfessors")
    public Flux<Professor> getAllProfessors() {
        logger.info("All professors were given");
        return pr.findAll();
    }

    // ---------------------------------------------------------------------------------//
    // AUTOMATIC CLIENT2 POPULATING DATABASE -------------------------------------------//

    @PostMapping("/add/student")
    Mono<Student> addStudent(@RequestBody Student s) {
        logger.info("Added student: " + s.getName());
        sr.save(s).subscribe();
        return Mono.just(s);
    }

    @PostMapping("/add/professor")
    Mono<Professor> addProfessor(@RequestBody Professor p) {
        logger.info("Added professor: " + p.getName());
        pr.save(p).subscribe();
        return Mono.just(p);
    }

    @PostMapping("/add/relationships")
    Mono<StudentProfessor> addRelationship(@RequestBody StudentProfessor sp) {
        logger.info("Realtionship: " + String.valueOf(sp.getProfessorId()) + " " + String.valueOf(sp.getStudentId()) + " added!");
        spr.save(sp).subscribe();
        return Mono.just(sp);
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

        studentProfessorRepository.saveAll(Arrays.asList(new StudentProfessor(1l,1l), new StudentProfessor(2l,1l)))
                .blockLast(Duration.ofSeconds(10));
        };

        
    }
}
