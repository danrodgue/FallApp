package com.example.Fallapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories("com.example.Fallapp.repository")
public class FallappApplication {

	public static void main(String[] args) {
		SpringApplication.run(FallappApplication.class, args);
	}

}

