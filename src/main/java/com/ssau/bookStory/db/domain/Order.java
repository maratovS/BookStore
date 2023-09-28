package com.ssau.bookStory.db.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ORDER_DS")
public class Order {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    private Date date;

    @ManyToMany(mappedBy = "orders")
    private Set<Book> books;

    @ManyToOne
    private User user;
}
