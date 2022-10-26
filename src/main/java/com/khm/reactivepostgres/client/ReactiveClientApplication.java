package com.khm.reactivepostgres.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toMap;
import static java.util.Map.Entry.comparingByValue;


import com.khm.reactivepostgres.entity.Event;
import com.khm.reactivepostgres.entity.Professor;
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

    // ---------------------------------------------------------------------------------//
    // CLIENT FEATURES -----------------------------------------------------------------//

    @Bean
    CommandLineRunner demo(WebClient client){

        return args -> {

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
                .doOnNext(cr -> System.out.println("Name: " + cr.getName() + " Credits:" + cr.getCredits()))
                .blockLast();

            //FEATURE 4
            System.out.println("--- 4. Courses completed ---");

            client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .doOnNext(cr -> System.out.println("Name: " + cr.getName() + " Number Courses:" + cr.getCredits()/6))
                .blockLast();

            //FEATURE 5
            System.out.println("--- 5. Students in last year ---");

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

            
            //FEATURE 6
            System.out.println("--- 6. Student grades average ---");

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

            //FEATURE 7
            System.out.println("--- 7. Student who finished grades average ---");
        
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
            
            //FEATURE 8
            System.out.println("--- 8. Average grade for finished students --- " );
            System.out.println(String.valueOf(mean2));        ;

            Long fluxSize = count2.block();

            stream2
                .map(x -> calculateStandardDeviation(x.getGrade(), mean2))
                .reduce((t, u) -> Double.sum(t, u))
                .map(x ->  Math.sqrt(x/fluxSize))
                .doOnNext(cr-> System.out.println("Standard deviation: " + String.valueOf(cr)))
                .block();
            
            //FEATURE 9
            System.out.println("--- 9. Average number of professors per student ---");

            Flux<Student> student_stream = client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class);
            
            Mono<Long> studentQuantity =  student_stream
                                        .count();

            Mono<Long> studentProfessorsQuantity = student_stream.
                                            flatMap((s) -> { return client
                                                                .get()
                                                                .uri("/get/studentProf/{id}", s.getId())
                                                                .accept(MediaType.TEXT_EVENT_STREAM)
                                                                .retrieve()
                                                                .bodyToFlux(Long.class);})
                                            .count();

            System.out.println( ((float) studentProfessorsQuantity.block() / (float)studentQuantity.block() ));

            //FEATURE 10
            System.out.println("--- 10. Name and number of students per professor ---");
            
            Flux<Student> student_stream3 = client
                                            .get()
                                            .uri("/student/getStudents")
                                            .accept(MediaType.TEXT_EVENT_STREAM)
                                            .retrieve()
                                            .bodyToFlux(Student.class);

            
            Map<String,List<Student>> studentProfessorLinks =  new HashMap<String,List<Student>>();

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
                                    .flatMap(x-> client
                                            .get()
                                            .uri("/get/professor/{id}", x)
                                            .accept(MediaType.TEXT_EVENT_STREAM)
                                            .retrieve()
                                            .bodyToFlux(Professor.class))
                                    .doOnNext(p-> {
                                
                                        studentProfessorLinks.get(p.getName()).add(s);
  
                                    }).subscribe()).blockLast();
        
        Thread.sleep(1000);
        //Adapted from https://stackoverflow.com/questions/30853117/how-can-i-sort-a-map-based-upon-on-the-size-of-its-collection-values
        Map<String,List<Student>> sorted = studentProfessorLinks.entrySet().stream()
                                    .sorted((l1,l2) -> l2.getValue().size()-l1.getValue().size())
                                    .collect(toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (a, b) -> { throw new AssertionError(); },
                                        LinkedHashMap::new
                                    )); 
                        
        sorted.forEach((k, v) ->{
                String aux = "";
                for(Student s : v){
                    aux +=  s.getName()+"|";
                }
                System.out.println("Professor:" + k + "|Number Students: " + v.size() +  "|Students:" + aux);});


            //FEATURE 11
            System.out.println("--- 11. Complete data of all students ---");
            Flux<Student> student_stream2 = client
                            .get()
                            .uri("/student/getStudents")
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .retrieve()
                            .bodyToFlux(Student.class);
            
            Flux<String> strings = student_stream2
                    .flatMap( (s) ->{
                        Mono<String> a = Mono.just("Name: "+ s.getName() + "|Birthdate: " + s.getBirthdate() + "|Credits: " + s.getCredits() + "|Grades: " + s.getGrade() + "|Professors:");

                        Flux<Long> teachersId = client
                                    .get()
                                    .uri("/get/studentProf/{id}", s.getId())
                                    .accept(MediaType.TEXT_EVENT_STREAM)
                                    .retrieve()
                                    .bodyToFlux(Long.class);

                        return Mono.zip(a, teachersId
                                            .flatMap((x) ->  client
                                                            .get()
                                                            .uri("/get/professor/{id}", x)
                                                            .accept(MediaType.TEXT_EVENT_STREAM)
                                                            .retrieve()
                                                            .bodyToFlux(Professor.class))
                                                            .map(p->p.getName())
                                            .collectList()
                                            .flatMap(l -> {
                                                if(l.isEmpty()) return Mono.just(" No professors");
                                                return Mono.just(l);
                                            }))                                            

                                .map(x -> x.getT1() + x.getT2());
                        });
                        
            strings.doOnNext(s-> System.out.println(s)).blockLast();
              
            
            System.out.println("--- 12. Oldest student ---");

            client
                .get()
                .uri("/student/getStudents")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(Student.class)
                .reduce(this::oldestDate)
                .doOnNext(cr -> System.out.println(cr.getName() + " (" + cr.getBirthdate() + ")"))
                .block();

        };
        
    }

    // ---------------------------------------------------------------------------------//
    // CLIENT FUNCTIONS ----------------------------------------------------------------//

    public float calculateAverage(int sum, long size){
        return (float)sum/size;
    }

    public double calculateStandardDeviation(int grade, float mean){
        return Math.pow(grade - mean, 2);

    }

    public Student oldestDate(Student s1, Student s2){

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





