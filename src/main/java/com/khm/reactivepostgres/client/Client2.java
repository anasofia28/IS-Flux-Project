package com.khm.reactivepostgres.client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import com.github.javafaker.Faker;
import com.khm.reactivepostgres.entity.StudentProfessor;
import org.springframework.web.reactive.function.client.WebClient;
import com.khm.reactivepostgres.entity.Student;
import com.khm.reactivepostgres.entity.Professor;
import reactor.core.publisher.Mono;

public class Client2 {

    // ---------------------------------------------------------------------------------//
    // CLIENT2 CONNECTION --------------------------------------------------------------//

    static WebClient getWebClient() {

        WebClient.Builder webClientBuilder = WebClient.builder();
        return webClientBuilder.build();

    }

    public static void main(String[] args) {

        WebClient client = WebClient.create("http://localhost:8080");

        Scanner sc = new Scanner(System.in);

        System.out.println("n students: ");
        int n_students = sc.nextInt();
        System.out.println("n professors: ");
        int n_profs = sc.nextInt();
        System.out.println("n relations: ");
        int n_relations = sc.nextInt();

        System.out.println("\nGenerating students...");
        List<Student> students = addStudents(client, n_students);
        System.out.println("Students added!\n");
        System.out.println("Generating professors...");
        List<Professor> profs = addProfessors(client, n_profs);
        System.out.println("Professors added!\n");
        System.out.println("Generating relationships...");
        addRelationships(client, n_relations, students, profs);
        System.out.println("Relationships added!\n");

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
        if( Math.random() < 0.05 ) credits = 180;
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
                    .body(Mono.just(s), Student.class)
                    .retrieve()
                    .bodyToMono(Student.class)
                    .block();

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
                    .body(Mono.just(p), Professor.class)
                    .retrieve()
                    .bodyToMono(Professor.class)
                    .block();

            professors.add(p);

        }

        return professors;

    }

    public static void addRelationships(WebClient client, int quantity, List<Student> students, List<Professor> professors) {

        for (int i = 0; i < quantity; i++) {

            long randomNum = ThreadLocalRandom.current().nextInt(1, quantity);
            long randomNum2 = ThreadLocalRandom.current().nextInt(1, quantity);

            StudentProfessor sp = new StudentProfessor(randomNum, randomNum2);

            client.post()
                    .uri("/add/relationships")
                    .body(Mono.just(sp), StudentProfessor.class)
                    .retrieve()
                    .bodyToMono(StudentProfessor.class)
                    .block();

        }
    }

}