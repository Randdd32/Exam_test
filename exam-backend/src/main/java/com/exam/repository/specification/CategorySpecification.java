package com.exam.repository.specification;

import com.exam.core.util.CommonSpecificationUtils;
import com.exam.core.util.QueryUtils;
import com.exam.model.template.CategoryEntity;
import com.exam.web.dto.filter.CategoryFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class CategorySpecification {
    public static Specification<CategoryEntity> withFilters(CategoryFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String cleanSearch = QueryUtils.cleanSearchToken(filter.search());
            if (cleanSearch != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + cleanSearch + "%"));
            }

            CommonSpecificationUtils.addAuditDateFilters(predicates, root, cb,
                    filter.createdAfter(), filter.createdBefore(),
                    filter.updatedAfter(), filter.updatedBefore());

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    private CategorySpecification() {}
}
