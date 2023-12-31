package com.ssau.bookStory.api.impl;

import com.ssau.bookStory.api.generated.api.UserApi;
import com.ssau.bookStory.api.generated.model.CategoryDto;
import com.ssau.bookStory.api.generated.model.GetUserList200Response;
import com.ssau.bookStory.api.generated.model.SignInDto;
import com.ssau.bookStory.api.generated.model.UserDto;
import com.ssau.bookStory.db.domain.Category;
import com.ssau.bookStory.db.domain.Publisher;
import com.ssau.bookStory.db.domain.User;
import com.ssau.bookStory.db.repo.UserRepository;
import com.ssau.bookStory.exception.RecordNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.ssau.bookStory.api.generated.model.GetUserList200Response.SortDirEnum.fromValue;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

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
        ExampleMatcher modelMatcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withMatcher("model", ignoreCase());
        if (repo.exists(Example.of(User.builder()
                .email(userDto.getEmail())
                .phoneNumber(userDto.getPhoneNumber())
                .build(), modelMatcher))) {
            throw new RuntimeException("User exists");
        }
        User user = User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .patronymic(userDto.getPatronymic())
                .email(userDto.getEmail())
                .phoneNumber(userDto.getPhoneNumber())
                .password(userDto.getPassword())
                .build();
        user = repo.save(user);
        userDto.setId(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(user, UserDto.class));
    }

    @Override
    public ResponseEntity<UserDto> updateUser(Long id, UserDto userDto) {
        Optional<User> found = repo.findById(id);
        Optional<UserDto> body = found.map(toUpdate -> {
            toUpdate.setFirstName(userDto.getFirstName());
            toUpdate.setLastName(userDto.getLastName());
            toUpdate.setPatronymic(userDto.getPatronymic());
            toUpdate.setEmail(userDto.getEmail());
            toUpdate.setPhoneNumber(userDto.getPhoneNumber());
            toUpdate.setPassword(userDto.getPassword());
            toUpdate = repo.save(toUpdate);
            return mapper.map(toUpdate, UserDto.class);
        });
        return ResponseEntity.ok(body.orElseThrow(() -> new RecordNotFoundException(User.class.getSimpleName(), String.format("id=%d", id))));
    }

    @Override
    public ResponseEntity<UserDto> signInUser(SignInDto signInDto) {
        User found = repo.findByEmailAndPassword(signInDto.getEmail(), signInDto.getPassword()).orElseThrow(() -> new RecordNotFoundException(User.class.getSimpleName(), String.format("email=%s", signInDto.getEmail())));
        return ResponseEntity.ok(mapper.map(found, UserDto.class));
    }
}
