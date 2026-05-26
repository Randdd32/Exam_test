package com.exam.service.template;

import com.exam.core.log.NoLogging;
import com.exam.model.auth.UserEntity;
import com.exam.model.template.ItemEntity;
import com.exam.repository.specification.ItemSpecification;
import com.exam.repository.template.CategoryRepository;
import com.exam.repository.template.ItemRepository;
import com.exam.repository.template.TagRepository;
import com.exam.service.AbstractCrudService;
import com.exam.service.auth.UserService;
import com.exam.web.dto.filter.ItemFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class ItemService extends AbstractCrudService<ItemEntity, ItemRepository> {
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public ItemService(ItemRepository repository, UserService userService, CategoryRepository categoryRepository, TagRepository tagRepository) {
        super(repository, ItemEntity.class);
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @NoLogging
    @Transactional(readOnly = true)
    public Page<ItemEntity> getAll(ItemFilter filter, Pageable pageable) {
        Specification<ItemEntity> spec = ItemSpecification.withFilters(filter);
        return repository.findAll(spec, pageable);
    }

    @Transactional
    public ItemEntity createFromDto(ItemEntity entity, Long authorId, Long categoryId, List<Long> tagIds) {
        validate(entity, null);

        UserEntity author = userService.getById(authorId);
        entity.setAuthor(author);

        if (categoryId != null) {
            entity.setCategory(categoryRepository.findById(categoryId).orElse(null));
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            entity.setTags(new HashSet<>(tagRepository.findAllById(tagIds)));
        }

        return repository.save(entity);
    }

    @Transactional
    public ItemEntity updateFromDto(Long id, ItemEntity updatedEntity, Long categoryId, List<Long> tagIds, Long currentUserId, boolean isAdmin) {
        ItemEntity existing = getById(id);

        if (!existing.getAuthor().getId().equals(currentUserId) && !isAdmin) {
            throw new AccessDeniedException("You can only edit your own items");
        }

        validate(updatedEntity, id);
        updateFields(existing, updatedEntity);

        if (categoryId != null) {
            existing.setCategory(categoryRepository.findById(categoryId).orElse(null));
        } else {
            existing.setCategory(null);
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            existing.setTags(new HashSet<>(tagRepository.findAllById(tagIds)));
        } else {
            existing.getTags().clear();
        }

        return repository.save(existing);
    }

    @Transactional
    public void deleteItem(Long id, Long currentUserId, boolean isAdmin) {
        ItemEntity item = getById(id);

        if (!item.getAuthor().getId().equals(currentUserId) && !isAdmin) {
            throw new AccessDeniedException("You can only delete your own items");
        }

        repository.delete(item);
    }

    @Override
    protected void validate(ItemEntity entity, Long id) {
        validateStringField(entity.getTitle(), "Title");
        if (entity.getValue() != null && entity.getValue() < 0) {
            throw new IllegalArgumentException("Value cannot be negative");
        }
    }

    @Override
    protected void updateFields(ItemEntity existing, ItemEntity updated) {
        existing.setTitle(updated.getTitle().trim());
        existing.setDescription(updated.getDescription() != null ? updated.getDescription().trim() : null);
        existing.setValue(updated.getValue());
    }
}
