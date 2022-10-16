package com.khm.reactivepostgres.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khm.reactivepostgres.repository.MemberRepository;
import com.khm.reactivepostgres.repository.ProfessorRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/professor")
@RequiredArgsConstructor
public class ProfessorController {
    private final ProfessorRepository studentRepository; 

}
