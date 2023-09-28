package com.ssau.bookStory.api.impl;

import com.ssau.bookStory.api.generated.api.OrderApi;
import com.ssau.bookStory.api.generated.model.GetOrderList200Response;
import com.ssau.bookStory.api.generated.model.GetPublisherList200Response;
import com.ssau.bookStory.api.generated.model.OrderDto;
import com.ssau.bookStory.api.generated.model.PublisherDto;
import com.ssau.bookStory.db.domain.Order;
import com.ssau.bookStory.db.domain.Publisher;
import com.ssau.bookStory.db.repo.OrderRepository;
import com.ssau.bookStory.exception.RecordNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.ssau.bookStory.api.generated.model.GetOrderList200Response.SortDirEnum.fromValue;

@RestController
public class OrderApiImpl implements OrderApi {

    @Autowired
    private OrderRepository repo;

    @Autowired
    private ModelMapper mapper;

    @Override
    public ResponseEntity<Void> deleteOrderById(Long id) {
        Order found = repo.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(Order.class.getSimpleName(), String.format("id=%d", id)));
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
    public ResponseEntity<GetOrderList200Response> getOrderList(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Page<Order> page = repo.findAll(PageRequest.of(pageNumber, pageSize, Sort.Direction.valueOf(sortDir), sortBy));
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
        return OrderApi.super.postOrder(orderDto);
    }

    @Override
    public ResponseEntity<OrderDto> updateOrder(Long id, OrderDto orderDto) {
        return OrderApi.super.updateOrder(id, orderDto);
    }
}
