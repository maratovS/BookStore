package com.ssau.bookStory.api.impl;

import com.ssau.bookStory.api.generated.api.CategoryApi;
import com.ssau.bookStory.api.generated.model.CategoryDto;
import com.ssau.bookStory.api.generated.model.GetCategoryList200Response;
import com.ssau.bookStory.db.domain.Category;
import com.ssau.bookStory.db.repo.CategoryRepository;
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

import static com.ssau.bookStory.api.generated.model.GetCategoryList200Response.SortDirEnum.fromValue;

@RestController
public class CategoryApiImpl implements CategoryApi {

    @Autowired
    private CategoryRepository repo;

    @Autowired
    private ModelMapper mapper;

    @Override
    public ResponseEntity<GetCategoryList200Response> getCategoryList(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Page<Category> page = repo.findAll(PageRequest.of(pageNumber, pageSize, Sort.Direction.valueOf(sortDir), sortBy));
        Sort.Order order = page.getSort().stream().findFirst().orElse(Sort.Order.asc("id"));
        GetCategoryList200Response body = new GetCategoryList200Response().sortField(order.getProperty())
                .sortDir(fromValue(order.getDirection().name()))
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalRows(page.getTotalElements())
                .records(page.get().map(record -> mapper.map(record, CategoryDto.class)).collect(Collectors.toList()));
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<Void> deleteCategoryById(Long id) {
        Category found = repo.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(Category.class.getSimpleName(), String.format("id=%d", id)));
        repo.delete(found);
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<CategoryDto> getCategoryId(Long id) {
        Optional<Category> found = repo.findById(id);
        return ResponseEntity.ok(found.map(category -> mapper.map(found, CategoryDto.class))
                .orElseThrow(() -> new RecordNotFoundException(Category.class.getSimpleName(), String.format("id=%d", id))));
    }

    @Override
    public ResponseEntity<CategoryDto> postCategory(CategoryDto categoryDto) {
        Category category = Category.builder()
                .name(categoryDto.getName())
                .build();
        category = repo.save(category);
        categoryDto.setId(category.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(category, CategoryDto.class));
    }

    @Override
    public ResponseEntity<CategoryDto> updateCategory(Long id, CategoryDto categoryDto) {
        Optional<Category> found = repo.findById(id);
        Optional<CategoryDto> body = found.map(toUpdate -> {
            toUpdate.setName(categoryDto.getName());
            toUpdate = repo.save(toUpdate);
            return mapper.map(toUpdate, CategoryDto.class);
        });
        return ResponseEntity.ok(body.orElseThrow(() -> new RecordNotFoundException(Category.class.getSimpleName(), String.format("id=%d", id))));

    }
}
