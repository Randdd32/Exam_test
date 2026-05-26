package com.exam.web.controller.template;

import com.exam.core.config.Constants;
import com.exam.core.log.NoLogging;
import com.exam.model.template.TagEntity;
import com.exam.service.template.TagService;
import com.exam.web.controller.AbstractCrudController;
import com.exam.web.dto.filter.TagFilter;
import com.exam.web.dto.pagination.PageDto;
import com.exam.web.dto.template.TagDto;
import com.exam.web.mapper.pagination.PageDtoMapper;
import com.exam.web.mapper.template.TagMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.API_URL + "/tags")
public class TagController extends AbstractCrudController<TagEntity, TagDto, TagService> {
    public TagController(TagService service, TagMapper mapper) {
        super(service, mapper::toDto, mapper::toEntity);
    }

    @NoLogging
    @GetMapping
    public PageDto<TagDto> getAll(
            @ModelAttribute TagFilter filter,
            @PageableDefault(size = Constants.DEFAULT_PAGE_SIZE, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageDtoMapper.toDto(service.getAll(filter, pageable), toDtoMapper);
    }
}
