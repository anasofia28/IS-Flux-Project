package com.khm.reactivepostgres.service.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khm.reactivepostgres.entity.Professor;
import com.khm.reactivepostgres.service.repository.ProfessorRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/professor")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorRepository professorRepository; 

    // Create professor
    @PostMapping
    public Mono<Professor> createprofessor(@RequestBody Professor prof) {
      return professorRepository.save(prof);
    }

    // Read all professors
    @GetMapping
    public Flux<Professor> getAllProfessors() {
      return professorRepository.findAll();
    }

    // Read specific professor
    @GetMapping(value = "/{name}")
    public Mono<Professor> getOne(@PathVariable String name) {
      return professorRepository.findByName(name);
    }

    // Update specific student
    @PutMapping
    public Mono<Professor> updateProfessor(@RequestBody Professor prof) {
        return professorRepository
            .findByName(prof.getName())
            .flatMap(studentResult -> professorRepository.save(prof));
    }

    // Delete specific student
    @DeleteMapping
    public Mono<Void> deleteProfessor(@RequestBody Professor prof) {
        return professorRepository.deleteById(prof.getId());
    }
  
}
