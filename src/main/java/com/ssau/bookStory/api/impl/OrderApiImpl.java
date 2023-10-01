package com.ssau.bookStory.api.impl;

import com.ssau.bookStory.api.generated.api.OrderApi;
import com.ssau.bookStory.api.generated.model.*;
import com.ssau.bookStory.db.domain.*;
import com.ssau.bookStory.db.domain.Order;
import com.ssau.bookStory.db.repo.BookRepository;
import com.ssau.bookStory.db.repo.OrderRepository;
import com.ssau.bookStory.db.repo.UserRepository;
import com.ssau.bookStory.exception.RecordNotFoundException;
import jakarta.persistence.criteria.*;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.ssau.bookStory.api.generated.model.GetOrderList200Response.SortDirEnum.fromValue;

@RestController
public class OrderApiImpl implements OrderApi {

    @Autowired
    private OrderRepository repo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ModelMapper mapper;

    @Override
    public ResponseEntity<Void> deleteOrderById(Long id) {
        Order found = repo.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(Order.class.getSimpleName(), String.format("id=%d", id)));
        found.getBooks().forEach(book -> book.getOrders().remove(found));
        found.setBooks(new HashSet<>());
        repo.delete(found);
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<OrderDto> getOrderId(Long id) {
        Optional<Order> found = repo.findById(id);
        return ResponseEntity.ok(found.map(publisher -> mapper.map(found, OrderDto.class))
                .orElseThrow(() -> new RecordNotFoundException(Order.class.getSimpleName(), String.format("id=%d", id))));
    }

    @Override
    public ResponseEntity<GetOrderList200Response> getOrderList(Integer pageNumber, Integer pageSize, String sortBy, String sortDir, @RequestParam(value = "filter", required = false) FilterDto filter) {
        Specification<Order> specification = (root, query, criteriaBuilder) -> {
            if (filter != null) {
                if (filter.getUserId() != null) {
                    Join<Order, User> userJoin = root.join("user");
                    return criteriaBuilder.equal(userJoin.get("user"), filter.getUserId());
                }
            }
            return null;
        };
        Page<Order> page = repo.findAll(specification, PageRequest.of(pageNumber, pageSize, Sort.Direction.valueOf(sortDir), sortBy));
        Sort.Order order = page.getSort().stream().findFirst().orElse(Sort.Order.asc("id"));
        GetOrderList200Response body = new GetOrderList200Response().sortField(order.getProperty())
                .sortDir(fromValue(order.getDirection().name()))
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalRows(page.getTotalElements())
                .records(page.get().map(record -> mapper.map(record, OrderDto.class)).collect(Collectors.toList()));
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<OrderDto> postOrder(OrderDto orderDto) {
        Order order = Order.builder()
                .date(Date.from(orderDto.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();
        User userFound = userRepository.findById(orderDto.getUser().getId()).orElseThrow(() -> new RecordNotFoundException(User.class.getSimpleName(), String.format("id=%d", orderDto.getUser().getId())));
        Set<Book> booksFound = orderDto.getBooks().stream().map(bookDto -> bookRepository.findById(bookDto.getId()).orElseThrow(() -> new RecordNotFoundException(User.class.getSimpleName(), String.format("id=%d", orderDto.getUser().getId())))).collect(Collectors.toSet());
        order.setUser(userFound);
        order.setBooks(booksFound);
        order = repo.save(order);
        Order finalOrder = order;
        booksFound.forEach(book -> book.getOrders().add(finalOrder));
        order = repo.save(order);
        orderDto.setId(order.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(order, OrderDto.class));
    }

    @Override
    public ResponseEntity<OrderDto> updateOrder(Long id, OrderDto orderDto) {
        Optional<Order> found = repo.findById(id);
        Optional<OrderDto> body = found.map(toUpdate -> {
            toUpdate.setDate(Date.from(orderDto.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            toUpdate = repo.save(toUpdate);
            User userFound = userRepository.findById(orderDto.getUser().getId()).orElseThrow(() -> new RecordNotFoundException(User.class.getSimpleName(), String.format("id=%d", orderDto.getUser().getId())));
            Set<Book> booksFound = orderDto.getBooks().stream().map(bookDto -> bookRepository.findById(bookDto.getId()).orElseThrow(() -> new RecordNotFoundException(User.class.getSimpleName(), String.format("id=%d", orderDto.getUser().getId())))).collect(Collectors.toSet());
            toUpdate.setUser(userFound);
            toUpdate.setBooks(booksFound);
            return mapper.map(toUpdate, OrderDto.class);
        });
        return ResponseEntity.ok(body.orElseThrow(() -> new RecordNotFoundException(Order.class.getSimpleName(), String.format("id=%d", id))));

    }
}
