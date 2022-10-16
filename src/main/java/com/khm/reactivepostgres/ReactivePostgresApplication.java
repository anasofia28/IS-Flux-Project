package com.khm.reactivepostgres;

import io.r2dbc.spi.ConnectionFactory;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;



import com.khm.reactivepostgres.entity.Member;
import com.khm.reactivepostgres.entity.Student;
import com.khm.reactivepostgres.repository.MemberRepository;
import com.khm.reactivepostgres.repository.StudentRepository;

import java.time.Duration;


@SpringBootApplication
@EnableR2dbcAuditing
public class ReactivePostgresApplication {

  private static final Logger log = LoggerFactory.getLogger(ReactivePostgresApplication.class);
  public static void main(String[] args) {
    SpringApplication.run(ReactivePostgresApplication.class, args);
  }

  @Bean
  ConnectionFactoryInitializer initializer(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(connectionFactory);
    ResourceDatabasePopulator resource =
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
    initializer.setDatabasePopulator(resource);
    return initializer;
  }

  @Bean
  public CommandLineRunner demo(StudentRepository studentRepository) {

    
    return (args) -> {
      studentRepository.deleteAll().subscribe();
        // save a few customers
      studentRepository.saveAll(Arrays.asList(new Student( "Rodas", "16-10-2022", 0, 3.2f),
          new Student( "Edgar", "16-10-2022", 0, 3.2f),
          new Student("Alexy", "16-10-2022", 0, 3.2f),
          new Student( "Tatiana", "16-10-2022", 0, 3.2f),
          new Student( "Sofia","16-10-2022", 0, 3.2f)))
          .blockLast(Duration.ofSeconds(10));

      studentRepository.save(new Student("WQRWQERWQRWQR","16-10-2022", 0, 3.2f)).subscribe();
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
