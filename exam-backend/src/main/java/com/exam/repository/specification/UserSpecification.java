package com.exam.repository.specification;

import com.exam.core.util.CommonSpecificationUtils;
import com.exam.core.util.QueryUtils;
import com.exam.model.auth.UserEntity;
import com.exam.web.dto.filter.UserFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecification {
    public static Specification<UserEntity> withFilters(UserFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String cleanSearch = QueryUtils.cleanSearchToken(filter.search());
            if (cleanSearch != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), "%" + cleanSearch + "%"),
                        cb.like(cb.lower(root.get("email")), "%" + cleanSearch + "%")
                ));
            }

            CommonSpecificationUtils.addInFilter(predicates, root, "role", filter.roles());

            CommonSpecificationUtils.addAuditDateFilters(predicates, root, cb, filter.createdAfter(), filter.createdBefore(),
                    filter.updatedAfter(), filter.updatedBefore());

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private UserSpecification() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
