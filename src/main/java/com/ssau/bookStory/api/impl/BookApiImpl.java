package com.ssau.bookStory.api.impl;

import com.ssau.bookStory.api.generated.api.BookApi;
import com.ssau.bookStory.api.generated.model.*;
import com.ssau.bookStory.api.generated.model.BookDto;
import com.ssau.bookStory.db.domain.*;
import com.ssau.bookStory.db.repo.*;
import com.ssau.bookStory.exception.RecordNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ssau.bookStory.api.generated.model.GetBookList200Response.SortDirEnum.fromValue;

@RestController
public class BookApiImpl implements BookApi {

    @Autowired
    private BookRepository repo;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private ConditionRepository conditionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelMapper mapper;

    @Override
    public ResponseEntity<Void> deleteBookById(Long id) {
        Book found = repo.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(Book.class.getSimpleName(), String.format("id=%d", id)));
        repo.delete(found);
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<BookDto> getBookId(Long id) {
        Optional<Book> found = repo.findById(id);
        return ResponseEntity.ok(found.map(book -> mapper.map(found, BookDto.class))
                .orElseThrow(() -> new RecordNotFoundException(Book.class.getSimpleName(), String.format("id=%d", id))));
    }

    @Override
    public ResponseEntity<GetBookList200Response> getBookList(Integer pageNumber, Integer pageSize, String sortBy, String sortDir, @RequestParam(value = "filter", required = false) FilterDto filter) {
        Page<Book> page = repo.findAll(PageRequest.of(pageNumber, pageSize, Sort.Direction.valueOf(sortDir), sortBy));
        Sort.Order order = page.getSort().stream().findFirst().orElse(Sort.Order.asc("id"));
        GetBookList200Response body = new GetBookList200Response().sortField(order.getProperty())
                .sortDir(fromValue(order.getDirection().name()))
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalRows(page.getTotalElements())
                .records(page.get().map(record -> mapper.map(record, BookDto.class)).collect(Collectors.toList()));
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<BookDto> postBook(BookDto bookDto) {
        Author authorFound = authorRepository.findById(bookDto.getAuthor().getId())
                .orElseThrow(() -> new RecordNotFoundException(Author.class.getSimpleName(), String.format("id=%d", bookDto.getAuthor().getId())));
        Publisher publisherFound = publisherRepository.findById(bookDto.getPublisher().getId())
                .orElseThrow(() -> new RecordNotFoundException(Publisher.class.getSimpleName(), String.format("id=%d", bookDto.getPublisher().getId())));
        Condition conditionFound = conditionRepository.findById(bookDto.getCondition().getId())
                .orElseThrow(() -> new RecordNotFoundException(Condition.class.getSimpleName(), String.format("id=%d", bookDto.getCondition().getId())));
        Category categoryFound = categoryRepository.findById(bookDto.getCategory().getId())
                .orElseThrow(() -> new RecordNotFoundException(Category.class.getSimpleName(), String.format("id=%d", bookDto.getCategory().getId())));
        Book toCreate = Book.builder()
                .title(bookDto.getTitle())
                .description(bookDto.getDescription())
                .price(bookDto.getPrice())
                .poster(bookDto.getPoster())
                .publicationYear(bookDto.getPublicationYear())
                .author(authorFound)
                .publisher(publisherFound)
                .condition(conditionFound)
                .category(categoryFound)
                .build();
        toCreate = repo.save(toCreate);
        bookDto.setId(toCreate.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(toCreate, BookDto.class));
    }

    @Override
    public ResponseEntity<BookDto> updateBook(Long id, BookDto bookDto) {
        Book found = repo.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(Book.class.getSimpleName(), String.format("id=%d", id)));
        Author authorFound = authorRepository.findById(bookDto.getAuthor().getId())
                .orElseThrow(() -> new RecordNotFoundException(Author.class.getSimpleName(), String.format("id=%d", bookDto.getAuthor().getId())));
        Publisher publisherFound = publisherRepository.findById(bookDto.getPublisher().getId())
                .orElseThrow(() -> new RecordNotFoundException(Publisher.class.getSimpleName(), String.format("id=%d", bookDto.getPublisher().getId())));
        Condition conditionFound = conditionRepository.findById(bookDto.getCondition().getId())
                .orElseThrow(() -> new RecordNotFoundException(Condition.class.getSimpleName(), String.format("id=%d", bookDto.getCondition().getId())));
        Category categoryFound = categoryRepository.findById(bookDto.getCategory().getId())
                .orElseThrow(() -> new RecordNotFoundException(Category.class.getSimpleName(), String.format("id=%d", bookDto.getCategory().getId())));

        found.setTitle(bookDto.getTitle());
        found.setDescription(bookDto.getDescription());
        found.setPrice(bookDto.getPrice());
        found.setPoster(bookDto.getPoster());
        found.setPublicationYear(bookDto.getPublicationYear());
        found.setAuthor(authorFound);
        found.setPublisher(publisherFound);
        found.setCondition(conditionFound);
        found.setCategory(categoryFound);

        found = repo.save(found);
        return ResponseEntity.ok(mapper.map(found, BookDto.class));
    }
}
