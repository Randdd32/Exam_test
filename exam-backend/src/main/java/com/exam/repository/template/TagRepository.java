package com.exam.repository.template;

import com.exam.model.template.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Long>, JpaSpecificationExecutor<TagEntity> {
    Optional<TagEntity> findByNameIgnoreCase(String name);
}
