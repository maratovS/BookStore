package com.ssau.bookStory;

import com.ssau.bookStory.db.domain.Publisher;
import com.ssau.bookStory.db.domain.User;
import com.ssau.bookStory.db.repo.PublisherRepository;
import com.ssau.bookStory.db.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BookStoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookStoryApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(UserRepository userRepository) {
		return (String[] args) -> {
			if (userRepository.findByEmailAndPassword("admin", "admin").isEmpty()) {
				userRepository.save(User.builder()
						.firstName("admin")
						.lastName("admin")
						.patronymic("admin")
						.email("admin")
						.password("admin")
						.build());
			}
		};
	}
}
