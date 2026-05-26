package com.exam.service.template;

import com.exam.core.log.NoLogging;
import com.exam.model.template.TagEntity;
import com.exam.repository.specification.TagSpecification;
import com.exam.repository.template.TagRepository;
import com.exam.service.AbstractCrudService;
import com.exam.web.dto.filter.TagFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TagService extends AbstractCrudService<TagEntity, TagRepository> {
    public TagService(TagRepository repository) {
        super(repository, TagEntity.class);
    }

    @NoLogging
    @Transactional(readOnly = true)
    public Page<TagEntity> getAll(TagFilter filter, Pageable pageable) {
        Specification<TagEntity> spec = TagSpecification.withFilters(filter);
        return repository.findAll(spec, pageable);
    }

    @Override
    protected void validate(TagEntity entity, Long id) {
        validateStringField(entity.getName(), "Name");

        Optional<TagEntity> existing = repository.findByNameIgnoreCase(entity.getName().trim());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Tag with this name already exists");
        }
    }

    @Override
    protected void updateFields(TagEntity existing, TagEntity updated) {
        existing.setName(updated.getName().trim());
    }
}
