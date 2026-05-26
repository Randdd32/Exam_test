package com.exam.web.controller.template;

import com.exam.core.config.Constants;
import com.exam.core.log.NoLogging;
import com.exam.model.auth.UserEntity;
import com.exam.model.template.ItemEntity;
import com.exam.service.auth.UserService;
import com.exam.service.template.ItemService;
import com.exam.web.controller.AbstractReadController;
import com.exam.web.dto.filter.ItemFilter;
import com.exam.web.dto.pagination.PageDto;
import com.exam.web.dto.template.ItemCreateUpdateDto;
import com.exam.web.dto.template.ItemDto;
import com.exam.web.mapper.pagination.PageDtoMapper;
import com.exam.web.mapper.template.ItemMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constants.API_URL + "/items")
public class ItemController extends AbstractReadController<ItemEntity, ItemDto, ItemService> {
    private final UserService userService;
    private final ItemMapper mapper;

    public ItemController(ItemService service, ItemMapper mapper, UserService userService) {
        super(service, mapper::toDto);
        this.mapper = mapper;
        this.userService = userService;
    }

    // ПУБЛИЧНАЯ СТРАНИЦА: Чтение всех объектов (Страница Б из требований)
    @NoLogging
    @GetMapping
    public PageDto<ItemDto> getAllPublic(
            @ModelAttribute ItemFilter filter,
            @PageableDefault(size = Constants.DEFAULT_PAGE_SIZE, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageDtoMapper.toDto(service.getAll(filter, pageable), toDtoMapper);
    }

    // ПРИВАТНАЯ СТРАНИЦА: Чтение только "МОИХ" объектов (Страница А из требований)
    @NoLogging
    @GetMapping("/my")
    public PageDto<ItemDto> getMyItems(
            @ModelAttribute ItemFilter filter,
            @PageableDefault(size = Constants.DEFAULT_PAGE_SIZE, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth) {

        UserEntity user = userService.getByUsername(auth.getName());

        // Принудительно ставим authorId = ID текущего пользователя
        ItemFilter myFilter = new ItemFilter(
                filter.search(), user.getId(), filter.categoryId(), filter.tagIds(),
                filter.minValue(), filter.maxValue(),
                filter.createdAfter(), filter.createdBefore(),
                filter.updatedAfter(), filter.updatedBefore()
        );

        return PageDtoMapper.toDto(service.getAll(myFilter, pageable), toDtoMapper);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestBody @Valid ItemCreateUpdateDto dto, Authentication auth) {
        UserEntity user = userService.getByUsername(auth.getName());
        ItemEntity created = service.createFromDto(mapper.toEntity(dto), user.getId(), dto.categoryId(), dto.tagIds());
        return mapper.toDto(created);
    }

    @PutMapping("/{id}")
    public ItemDto update(@PathVariable Long id, @RequestBody @Valid ItemCreateUpdateDto dto, Authentication auth) {
        UserEntity user = userService.getByUsername(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

        ItemEntity updated = service.updateFromDto(id, mapper.toEntity(dto), dto.categoryId(), dto.tagIds(), user.getId(), isAdmin);
        return mapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        UserEntity user = userService.getByUsername(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

        service.deleteItem(id, user.getId(), isAdmin);
    }
}
