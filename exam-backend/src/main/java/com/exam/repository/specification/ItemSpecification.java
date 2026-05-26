package com.exam.repository.specification;

import com.exam.core.util.CommonSpecificationUtils;
import com.exam.core.util.QueryUtils;
import com.exam.model.template.ItemEntity;
import com.exam.web.dto.filter.ItemFilter;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ItemSpecification {
    public static Specification<ItemEntity> withFilters(ItemFilter filter) {
        return (root, query, cb) -> {
            // Предотвращение N+1
            if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("author", JoinType.LEFT);
                root.fetch("category", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            String cleanSearch = QueryUtils.cleanSearchToken(filter.search());
            if (cleanSearch != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), "%" + cleanSearch + "%"),
                        cb.like(cb.lower(root.get("description")), "%" + cleanSearch + "%")
                ));
            }

            CommonSpecificationUtils.addEqualityFilter(predicates, root, cb, "author.id", filter.authorId());
            CommonSpecificationUtils.addEqualityFilter(predicates, root, cb, "category.id", filter.categoryId());

            // Many-To-Many filter from your Core
            CommonSpecificationUtils.addDictionaryFilter(predicates, root, "tags", filter.tagIds());

            CommonSpecificationUtils.addRangeFilter(predicates, root, cb, "value", filter.minValue(), filter.maxValue());
            CommonSpecificationUtils.addAuditDateFilters(predicates, root, cb,
                    filter.createdAfter(), filter.createdBefore(),
                    filter.updatedAfter(), filter.updatedBefore());

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    private ItemSpecification() {}
}
