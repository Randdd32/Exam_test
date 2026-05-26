package com.exam.service.template;

import com.exam.core.log.NoLogging;
import com.exam.model.template.CategoryEntity;
import com.exam.repository.specification.CategorySpecification;
import com.exam.repository.template.CategoryRepository;
import com.exam.service.AbstractCrudService;
import com.exam.web.dto.filter.CategoryFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CategoryService extends AbstractCrudService<CategoryEntity, CategoryRepository> {
    public CategoryService(CategoryRepository repository) {
        super(repository, CategoryEntity.class);
    }

    @NoLogging
    @Transactional(readOnly = true)
    public Page<CategoryEntity> getAll(CategoryFilter filter, Pageable pageable) {
        Specification<CategoryEntity> spec = CategorySpecification.withFilters(filter);
        return repository.findAll(spec, pageable);
    }

    @Override
    protected void validate(CategoryEntity entity, Long id) {
        validateStringField(entity.getName(), "Name");

        Optional<CategoryEntity> existing = repository.findByNameIgnoreCase(entity.getName().trim());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Category with this name already exists");
        }
    }

    @Override
    protected void updateFields(CategoryEntity existing, CategoryEntity updated) {
        existing.setName(updated.getName().trim());
    }
}
