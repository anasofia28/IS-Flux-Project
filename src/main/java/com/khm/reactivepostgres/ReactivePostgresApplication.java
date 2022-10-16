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
import com.khm.reactivepostgres.repository.MemberRepository;



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
  public CommandLineRunner demo(MemberRepository memberRepository) {

    
    return (args) -> {
      memberRepository.deleteAll().subscribe();
        // save a few customers
      memberRepository.saveAll(Arrays.asList(new Member( "Rodas", "Ferreira"),
          new Member( "Edgar", "Duarte"),
          new Member("Alexy", "Almeida"),
          new Member( "Tatiana", "Almeida"),
          new Member( "Sofia", "Neves")))
          .blockLast(Duration.ofSeconds(10));

      memberRepository.save(new Member("WQRWQERWQRWQR", "Barroso")).subscribe();
      // fetch all customers
      log.info("Customers found with findAll():");
      log.info("-------------------------------");
      memberRepository.findAll().doOnNext(customer -> {
        log.info(customer.toString());
      }).blockLast(Duration.ofSeconds(10));

      log.info("");

            // fetch an individual customer by ID
      memberRepository.findById(1L).doOnNext(customer -> {
        log.info("Customer found with findById(1L):");
        log.info("--------------------------------");
        log.info(customer.toString());
        log.info("");
      }).block(Duration.ofSeconds(10));


      // fetch customers by last name
      log.info("Customer found with findByLastName('Almeida'):");
      log.info("--------------------------------------------");
      memberRepository.findByLastName("Almeida").doOnNext(bauer -> {
        log.info(bauer.toString());
        }).blockLast(Duration.ofSeconds(10));;
        log.info("");
    };
}
}
