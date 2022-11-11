package com.khm.reactivepostgres.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import static java.util.stream.Collectors.toMap;

import com.khm.reactivepostgres.entity.Professor;
import com.khm.reactivepostgres.entity.Student;


import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.core.publisher.Flux;


public class ReactiveClientApplication {


    static WebClient client() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        return webClientBuilder.build();
    }

    // ---------------------------------------------------------------------------------//
    // CLIENT FEATURES -----------------------------------------------------------------//

    public static void main(String[] args) {

        WebClient client = WebClient.create("http://localhost:8080");

        //FEATURE 1
        System.out.println("--- 1. Students name and birthdates ---");

        client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .doOnNext(cr -> System.out.println("Name: " + cr.getName() + " BirthDate: " + cr.getBirthdate()))
                .blockLast();

        //FEATURE 2
        System.out.println("--- 2. Number of Students ---");

        client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .count()
                .doOnNext(cr -> System.out.println("Count " + cr))
                .block();

        //FEATURE 3
        System.out.println("--- 3. Students still active ---");

        client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .filter(gr -> gr.getCredits() < 180)
                .count()
                .doOnNext(cr -> System.out.println("Count " + cr))
                .block();

        //FEATURE 4
        System.out.println("--- 4. Courses completed ---");

        client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .map(cr -> cr.getCredits())
                .reduce(Integer::sum)
                .doOnNext(s -> System.out.println("Amount: " + s / 6))
                .block();

        //FEATURE 5
        System.out.println("--- 5. Students in last year ---");

        client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .filter(gr -> gr.getCredits() < 180 && gr.getCredits() >= 120)
                .sort((s1, s2) -> {
                    return s2.getCredits() - s1.getCredits();
                })
                .doOnNext(cr ->  System.out.println("Name: " + cr.getName() + " Birthdate: " + cr.getBirthdate() + " Grade: " + cr.getGrade() + " Credits: " + cr.getCredits()))
                .blockLast();


        //FEATURE 6
        System.out.println("--- 6. Student grades average and std ---");

        Flux<Student> stream = client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class);


        Mono<Integer> sum = stream
                .map(s -> s.getGrade())
                .reduce(0, (x1, x2) -> x1 + x2);

        Mono<Long> count = stream
                .count();

        Float mean = Flux.zip(sum, count)
                .map(x -> calculateAverage(x.getT1(), x.getT2()))
                .doOnNext(cr -> System.out.println("Average grades: " + String.valueOf(cr)))
                .blockLast();

        Long fluxSize0 = count.block();

        stream
                .map(x -> calculateStandardDeviation(x.getGrade(), mean))
                .reduce(Double::sum)
                .map(x -> Math.sqrt(x / fluxSize0))
                .doOnNext(cr -> System.out.println("Standard deviation: " + String.valueOf(cr)))
                .block();

        //FEATURE 7
        System.out.println("--- 7. Student who finished grades average and std ---");

        Flux<Student> stream2 = client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .filter(gr -> gr.getCredits() == 180);

        Mono<Integer> sum2 = stream2
                .map(Student::getGrade)
                .reduce(0, Integer::sum);

        Mono<Long> count2 = stream2
                .count();

        Float mean2 = Flux.zip(sum2, count2)
                .map(x -> calculateAverage(x.getT1(), x.getT2()))
                .blockLast();

        System.out.println("Mean: " + String.valueOf(mean2));

        Long fluxSize = count2.block();

        stream2
                .map(x -> calculateStandardDeviation(x.getGrade(), mean2))
                .reduce(Double::sum)
                .map(x -> Math.sqrt(x / fluxSize))
                .doOnNext(cr -> System.out.println("Standard deviation: " + String.valueOf(cr)))
                .block();

        //FEATURE 8
        System.out.println("--- 8. Eldest student ---");

        client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .reduce((s1, s2) ->oldestDate(s1,s2))
                .doOnNext(cr -> System.out.println(cr.getName() + " (" + cr.getBirthdate() + ")"))
                .block();

        //FEATURE 9
        System.out.println("--- 9. Average number of professors per student ---");

        Flux<Student> student_stream = client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class);

        Mono<Long> studentQuantity = student_stream
                .count();

        
        Mono<Long> studentProfessorsQuantity = student_stream.
                flatMap((s) -> {
                    return client
                            .get()
                            .uri("/get/studentProf/{id}", s.getId())
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .retrieve()
                            .bodyToFlux(Long.class);
                })
                .count()
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(4)));

        System.out.println(((float) studentProfessorsQuantity.block() / (float) studentQuantity.block()));

        //FEATURE 10
        System.out.println("--- 10. Name and number of students per professor ---");

        Flux<Student> student_stream3 = client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class);


        Map<String, List<Student>> studentProfessorLinks = new HashMap<String, List<Student>>();

        client.get()
                .uri("/get/allProfessors")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Professor.class)
                .doOnNext(p -> studentProfessorLinks.put(p.getName(), new ArrayList<Student>()))
                .blockLast();

        student_stream3.doOnNext(s -> client
                .get()
                .uri("/get/studentProf/{id}", s.getId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Long.class)
                .flatMap(x -> client
                        .get()
                        .uri("/get/professor/{id}", x)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .retrieve()
                        .bodyToFlux(Professor.class))
                .doOnNext(p -> {

                    studentProfessorLinks.get(p.getName()).add(s);

                }).subscribe()).blockLast();

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Adapted from https://stackoverflow.com/questions/30853117/how-can-i-sort-a-map-based-upon-on-the-size-of-its-collection-values
        Map<String, List<Student>> sorted = studentProfessorLinks.entrySet().stream()
                .sorted((l1, l2) -> l2.getValue().size() - l1.getValue().size())
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));

        sorted.forEach((k, v) -> {
            String aux = "";
            for (Student s : v) {
                aux += s.getName() + "|";
            }
            System.out.println("Professor:" + k + "|Number Students: " + v.size() + "|Students:" + aux);
        });


        //FEATURE 11
        System.out.println("--- 11. Complete data of all students ---");
        client .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .flatMap((s) -> {
                    Mono<String> a = Mono.just("Name: " + s.getName() + "|Birthdate: " + s.getBirthdate() + "|Credits: " + s.getCredits() + "|Grades: " + s.getGrade() + "|Professors:");

                    Flux<Long> teachersId = client
                            .get()
                            .uri("/get/studentProf/{id}", s.getId())
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .retrieve()
                            .bodyToFlux(Long.class);

                    return Mono.zip(a, teachersId
                                    .flatMap((x) -> client
                                            .get()
                                            .uri("/get/professor/{id}", x)
                                            .accept(MediaType.TEXT_EVENT_STREAM)
                                            .retrieve()
                                            .bodyToFlux(Professor.class))
                                    .map(p -> p.getName())
                                    .collectList()
                                    .flatMap(l -> {
                                        if (l.isEmpty()) return Mono.just(" No professors");
                                        return Mono.just(l);
                                    }))

                            .map(x -> x.getT1() + x.getT2());
                        })
                .doOnNext(s -> System.out.println(s)).blockLast();
        
        System.exit(0);
    
    
    }

    // ---------------------------------------------------------------------------------//
    // CLIENT FUNCTIONS ----------------------------------------------------------------//

    public static float calculateAverage(int sum, long size) {
        return (float) sum / size;
    }

    public static double calculateStandardDeviation(int grade, float mean) {
        return Math.pow(grade - mean, 2);

    }

    public static Student oldestDate(Student s1, Student s2) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date1 = new Date();
        Date date2 = new Date();

        try {
            date1 = formatter.parse(s1.getBirthdate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            date2 = formatter.parse(s2.getBirthdate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date1.before(date2)) return s1;
        else return s2;
    }

}





