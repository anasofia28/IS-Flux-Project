package com.khm.reactivepostgres.service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.khm.reactivepostgres.service.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final StudentRepository studentRepository; 
    
}
