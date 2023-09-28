package com.ssau.bookStory.db.repo;

import com.ssau.bookStory.db.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}