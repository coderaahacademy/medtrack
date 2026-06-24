package com.medtrack;

import com.medtrack.repository.RootRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = RootRepository.class)
public class MedtrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedtrackApplication.class, args);
    }
}
