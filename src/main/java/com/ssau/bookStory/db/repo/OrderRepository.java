package com.ssau.bookStory.db.repo;

import com.ssau.bookStory.db.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}