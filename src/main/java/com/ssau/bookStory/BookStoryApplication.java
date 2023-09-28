package com.ssau.bookStory;

import com.ssau.bookStory.db.domain.Publisher;
import com.ssau.bookStory.db.repo.PublisherRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BookStoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookStoryApplication.class, args);
	}


}
