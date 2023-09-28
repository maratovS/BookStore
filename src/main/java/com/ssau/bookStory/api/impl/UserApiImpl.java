package com.ssau.bookStory.api.impl;

import com.ssau.bookStory.api.generated.api.UserApi;
import com.ssau.bookStory.api.generated.model.GetUserList200Response;
import com.ssau.bookStory.api.generated.model.UserDto;
import com.ssau.bookStory.db.domain.Publisher;
import com.ssau.bookStory.db.domain.User;
import com.ssau.bookStory.db.repo.UserRepository;
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

import static com.ssau.bookStory.api.generated.model.GetUserList200Response.SortDirEnum.fromValue;

@RestController
public class UserApiImpl implements UserApi {

    @Autowired
    private UserRepository repo;

    @Autowired
    private ModelMapper mapper;

    @Override
    public ResponseEntity<Void> deleteUserById(Long id) {
        User found = repo.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(Publisher.class.getSimpleName(), String.format("id=%d", id)));
        repo.delete(found);
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<UserDto> getUserId(Long id) {
        Optional<User> found = repo.findById(id);
        return ResponseEntity.ok(found.map(publisher -> mapper.map(found, UserDto.class))
                .orElseThrow(() -> new RecordNotFoundException(User.class.getSimpleName(), String.format("id=%d", id))));
    }

    @Override
    public ResponseEntity<GetUserList200Response> getUserList(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Page<User> page = repo.findAll(PageRequest.of(pageNumber, pageSize, Sort.Direction.valueOf(sortDir), sortBy));
        Sort.Order order = page.getSort().stream().findFirst().orElse(Sort.Order.asc("id"));
        GetUserList200Response body = new GetUserList200Response().sortField(order.getProperty())
                .sortDir(fromValue(order.getDirection().name()))
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalRows(page.getTotalElements())
                .records(page.get().map(record -> mapper.map(record, UserDto.class)).collect(Collectors.toList()));
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<UserDto> postUser(UserDto userDto) {
        return UserApi.super.postUser(userDto);
    }

    @Override
    public ResponseEntity<UserDto> updateUser(Long id, UserDto userDto) {
        return UserApi.super.updateUser(id, userDto);
    }
}
