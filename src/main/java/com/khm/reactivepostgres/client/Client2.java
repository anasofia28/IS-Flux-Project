package com.khm.reactivepostgres.client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.github.javafaker.Faker;
import com.khm.reactivepostgres.client.ReactiveClientApplication;
import com.khm.reactivepostgres.entity.StudentProfessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import com.khm.reactivepostgres.entity.Student;
import com.khm.reactivepostgres.entity.Professor;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class Client2 {

    // ---------------------------------------------------------------------------------//
    // CLIENT2 CONNECTION --------------------------------------------------------------//

    public static void main(String[] args) {

        //TODO: connect client to server
        //TODO: retry connections when it fails

        new SpringApplicationBuilder(ReactiveClientApplication.class)
                .properties(Collections.singletonMap("server.port", "8082"))
                .run(args);

        WebClient client = WebClient.create("http://localhost:8080");

        System.out.println("Entrou no demo 2");
        List<Student> students = addStudents(client, 100);
        List<Professor> profs = addProfessors(client, 100);
        addRelationships(client, 100, students, profs);

    }


    // ---------------------------------------------------------------------------------//
    // POPULATE DATABASE ---------------------------------------------------------------//

    public static Student createRandomStudent() {

        Faker faker = new Faker();
        String name = faker.name().fullName();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date date = faker.date().birthday();
        String birthDate = sdf.format(date);
        int credits = faker.number().numberBetween(0, 180);
        int grade = faker.number().numberBetween(0, 20);

        return new Student(name, birthDate, credits, grade);

    }

    public static Professor createRandomProfessor() {

        Faker faker = new Faker();
        String name = faker.name().fullName();

        return new Professor(name);

    }

    public static List<Student> addStudents(WebClient client, int quantity) {

        List<Student> students = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            Student s = createRandomStudent();
            client.post()
                    .uri("/add/student")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .body(Mono.just(s), Student.class);
            students.add(s);
        }

        return students;

    }

    public static List<Professor> addProfessors(WebClient client, int quantity) {

        List<Professor> professors = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {

            Professor p = createRandomProfessor();
            client.post()
                    .uri("/add/professor")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .body(Mono.just(p), Professor.class);

            professors.add(p);

        }

        return professors;

    }

    public static void addRelationships(WebClient client, int quantity, List<Student> students, List<Professor> professors) {

        for (int i = 0; i < quantity; i++) {

            long randomNum = ThreadLocalRandom.current().nextInt(1, quantity);
            long randomNum2 = ThreadLocalRandom.current().nextInt(1, quantity);

            StudentProfessor sp = new StudentProfessor();
            sp.setProfessorId(randomNum);
            sp.setStudentId(randomNum2);

            client.post()
                    .uri("/add/relationships")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .body(Mono.just(sp), StudentProfessor.class);

        }
    }

}