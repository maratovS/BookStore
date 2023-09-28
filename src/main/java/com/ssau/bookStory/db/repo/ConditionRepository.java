package com.ssau.bookStory.db.repo;

import com.ssau.bookStory.db.domain.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}