package com.ssau.bookStory.db.repo;

import com.ssau.bookStory.db.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}