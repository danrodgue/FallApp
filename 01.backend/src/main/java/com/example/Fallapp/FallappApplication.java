package com.example.Fallapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.Fallapp.repository")
public class FallappApplication {

	public static void main(String[] args) {
		SpringApplication.run(FallappApplication.class, args);
	}

}

