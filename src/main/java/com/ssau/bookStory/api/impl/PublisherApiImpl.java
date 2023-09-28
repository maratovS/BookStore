package com.ssau.bookStory.api.impl;

import com.ssau.bookStory.api.generated.api.PublisherApi;
import com.ssau.bookStory.api.generated.model.GetPublisherList200Response;
import com.ssau.bookStory.api.generated.model.PublisherDto;
import com.ssau.bookStory.db.domain.Publisher;
import com.ssau.bookStory.db.repo.PublisherRepository;
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

import static com.ssau.bookStory.api.generated.model.GetPublisherList200Response.SortDirEnum.fromValue;

@RestController
public class PublisherApiImpl implements PublisherApi {

    @Autowired
    private PublisherRepository repo;

    @Autowired
    private ModelMapper mapper;


    @Override
    public ResponseEntity<Void> deletePublisherById(Long id) {
        Publisher found = repo.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(Publisher.class.getSimpleName(), String.format("id=%d", id)));
        repo.delete(found);
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<PublisherDto> getPublisherId(Long id) {
        Optional<Publisher> found = repo.findById(id);
        return ResponseEntity.ok(found.map(publisher -> mapper.map(found, PublisherDto.class))
                .orElseThrow(() -> new RecordNotFoundException(Publisher.class.getSimpleName(), String.format("id=%d", id))));
    }

    @Override
    public ResponseEntity<GetPublisherList200Response> getPublisherList(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Page<Publisher> page = repo.findAll(PageRequest.of(pageNumber, pageSize, Sort.Direction.valueOf(sortDir), sortBy));
        Sort.Order order = page.getSort().stream().findFirst().orElse(Sort.Order.asc("id"));
        GetPublisherList200Response body = new GetPublisherList200Response().sortField(order.getProperty())
                .sortDir(fromValue(order.getDirection().name()))
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalRows(page.getTotalElements())
                .records(page.get().map(record -> mapper.map(record, PublisherDto.class)).collect(Collectors.toList()));
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<PublisherDto> postPublisher(PublisherDto publisherDto) {
        Publisher publisher = Publisher.builder()
                .name(publisherDto.getName())
                .build();
        publisher = repo.save(publisher);
        publisherDto.setId(publisher.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(publisher, PublisherDto.class));
    }

    @Override
    public ResponseEntity<PublisherDto> updatePublisher(Long id, PublisherDto publisherDto) {
        Optional<Publisher> found = repo.findById(id);
        Optional<PublisherDto> body = found.map(toUpdate -> {
            toUpdate.setName(publisherDto.getName());
            toUpdate = repo.save(toUpdate);
            return mapper.map(toUpdate, PublisherDto.class);
        });
        return ResponseEntity.ok(body.orElseThrow(() -> new RecordNotFoundException(Publisher.class.getSimpleName(), String.format("id=%d", id))));
    }
}
