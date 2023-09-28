package com.ssau.bookStory.api.impl;

import com.ssau.bookStory.api.generated.api.ConditionApi;
import com.ssau.bookStory.api.generated.model.ConditionDto;
import com.ssau.bookStory.api.generated.model.GetConditionList200Response;
import com.ssau.bookStory.db.domain.Condition;
import com.ssau.bookStory.db.repo.ConditionRepository;
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

import static com.ssau.bookStory.api.generated.model.GetConditionList200Response.SortDirEnum.fromValue;


@RestController
public class ConditionApiImpl implements ConditionApi {

    @Autowired
    private ConditionRepository repo;

    @Autowired
    private ModelMapper mapper;

    @Override
    public ResponseEntity<Void> deleteConditionById(Long id) {
        Condition found = repo.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(Condition.class.getSimpleName(), String.format("id=%d", id)));
        repo.delete(found);
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<ConditionDto> getConditionId(Long id) {
        Optional<Condition> found = repo.findById(id);
        return ResponseEntity.ok(found.map(condition -> mapper.map(found, ConditionDto.class))
                .orElseThrow(() -> new RecordNotFoundException(Condition.class.getSimpleName(), String.format("id=%d", id))));
    }

    @Override
    public ResponseEntity<ConditionDto> postCondition(ConditionDto conditionDto) {
        Condition condition = Condition.builder()
                .name(conditionDto.getName())
                .build();
        condition = repo.save(condition);
        conditionDto.setId(condition.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(condition, ConditionDto.class));
    }

    @Override
    public ResponseEntity<ConditionDto> updateCondition(Long id, ConditionDto conditionDto) {
        Optional<Condition> found = repo.findById(id);
        Optional<ConditionDto> body = found.map(toUpdate -> {
            toUpdate.setName(conditionDto.getName());
            toUpdate = repo.save(toUpdate);
            return mapper.map(toUpdate, ConditionDto.class);
        });
        return ResponseEntity.ok(body.orElseThrow(() -> new RecordNotFoundException(Condition.class.getSimpleName(), String.format("id=%d", id))));
    }

    @Override
    public ResponseEntity<GetConditionList200Response> getConditionList(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Page<Condition> page = repo.findAll(PageRequest.of(pageNumber, pageSize, Sort.Direction.valueOf(sortDir), sortBy));
        Sort.Order order = page.getSort().stream().findFirst().orElse(Sort.Order.asc("id"));
        GetConditionList200Response body = new GetConditionList200Response().sortField(order.getProperty())
                .sortDir(fromValue(order.getDirection().name()))
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalRows(page.getTotalElements())
                .records(page.get().map(record -> mapper.map(record, ConditionDto.class)).collect(Collectors.toList()));
        return ResponseEntity.ok(body);
    }
}
