package com.khm.reactivepostgres.service.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khm.reactivepostgres.entity.Student;
import com.khm.reactivepostgres.service.repository.StudentRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentRepository studentRepository; 

    // Create student
    @PostMapping
    public Mono<Student> createStudent(@RequestBody Student student) {
      return studentRepository.save(student);
    }

    // Read all students
    @GetMapping
    public Flux<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Read specific student
    @GetMapping(value = "/{name}")
    public Mono<Student> getOne(@PathVariable String name) {
        return studentRepository.findByName(name);
    }

    // Update specific student
    @PutMapping
    public Mono<Student> updateStudent(@RequestBody Student student) {
      return studentRepository
          .findByName(student.getName())
          .flatMap(studentResult -> studentRepository.save(student));
    }
    
    // Delete specific student
    @DeleteMapping
    public Mono<Void> deleteStudent(@RequestBody Student student) {
      return studentRepository.deleteById(student.getId());
    }
}
