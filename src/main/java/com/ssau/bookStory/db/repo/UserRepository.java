package com.ssau.bookStory.db.repo;

import com.ssau.bookStory.db.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}