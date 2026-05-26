package com.exam.web.controller.template;

import com.exam.core.config.Constants;
import com.exam.core.log.NoLogging;
import com.exam.model.template.CategoryEntity;
import com.exam.service.template.CategoryService;
import com.exam.web.controller.AbstractCrudController;
import com.exam.web.dto.filter.CategoryFilter;
import com.exam.web.dto.pagination.PageDto;
import com.exam.web.dto.template.CategoryDto;
import com.exam.web.mapper.pagination.PageDtoMapper;
import com.exam.web.mapper.template.CategoryMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.API_URL + "/categories")
public class CategoryController extends AbstractCrudController<CategoryEntity, CategoryDto, CategoryService> {
    public CategoryController(CategoryService service, CategoryMapper mapper) {
        super(service, mapper::toDto, mapper::toEntity);
    }

    @NoLogging
    @GetMapping
    public PageDto<CategoryDto> getAll(
            @ModelAttribute CategoryFilter filter,
            @PageableDefault(size = Constants.DEFAULT_PAGE_SIZE, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageDtoMapper.toDto(service.getAll(filter, pageable), toDtoMapper);
    }
}
