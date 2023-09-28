package com.ssau.bookStory.api.impl;

import com.ssau.bookStory.api.generated.api.AuthorApi;
import com.ssau.bookStory.api.generated.model.AuthorDto;
import com.ssau.bookStory.api.generated.model.GetAuthorList200Response;
import com.ssau.bookStory.db.domain.Author;
import com.ssau.bookStory.db.repo.AuthorRepository;
import com.ssau.bookStory.exception.RecordNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.ssau.bookStory.api.generated.model.GetAuthorList200Response.SortDirEnum.fromValue;

@RestController
public class AuthorApiImpl implements AuthorApi {

    @Autowired
    private AuthorRepository repo;

    @Autowired
    private ModelMapper mapper;

    @Override
    public ResponseEntity<Void> deleteAuthorById(Long id) {
        Author found = repo.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(Author.class.getSimpleName(), String.format("id=%d", id)));
        repo.delete(found);
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<AuthorDto> getAuthorId(Long id) {
        Optional<Author> found = repo.findById(id);
        return ResponseEntity.ok(found.map(author -> mapper.map(found, AuthorDto.class))
                .orElseThrow(() -> new RecordNotFoundException(Author.class.getSimpleName(), String.format("id=%d", id))));
    }

    @Override
    public ResponseEntity<GetAuthorList200Response> getAuthorList(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Page<Author> page = repo.findAll(PageRequest.of(pageNumber, pageSize, Sort.Direction.valueOf(sortDir), sortBy));
        Sort.Order order = page.getSort().stream().findFirst().orElse(Sort.Order.asc("id"));
        GetAuthorList200Response body = new GetAuthorList200Response().sortField(order.getProperty())
                .sortDir(fromValue(order.getDirection().name()))
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalRows(page.getTotalElements())
                .records(page.get().map(record -> mapper.map(record, AuthorDto.class)).collect(Collectors.toList()));
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<AuthorDto> postAuthor(AuthorDto authorDto) {
        Author author = Author.builder()
                .firstName(authorDto.getFirstName())
                .secondName(authorDto.getSecondName())
                .build();
        author = repo.save(author);
        authorDto.setId(author.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(author, AuthorDto.class));
    }

    @Override
    public ResponseEntity<AuthorDto> updateAuthor(Long id, AuthorDto authorDto) {
        Optional<Author> found = repo.findById(id);
        Optional<AuthorDto> body = found.map(toUpdate -> {
            toUpdate.setFirstName(authorDto.getFirstName());
            toUpdate.setSecondName(authorDto.getSecondName());
            toUpdate = repo.save(toUpdate);
            return mapper.map(toUpdate, AuthorDto.class);
        });
        return ResponseEntity.ok(body.orElseThrow(() -> new RecordNotFoundException(Author.class.getSimpleName(), String.format("id=%d", id))));

    }
}
