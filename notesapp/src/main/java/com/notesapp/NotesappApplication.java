package com.notesapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotesappApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotesappApplication.class, args);
	}

}
