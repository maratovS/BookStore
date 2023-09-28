package com.ssau.bookStory.db.repo;

import com.ssau.bookStory.db.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}