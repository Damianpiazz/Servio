package com.servio.backend.identity.repository;

import com.servio.backend.identity.dto.request.AdminUserFilterRequest;
import com.servio.backend.identity.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> withFilters(AdminUserFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("email")),
                        "%" + filter.getEmail().toLowerCase() + "%"
                ));
            }

            if (filter.getFirstname() != null && !filter.getFirstname().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("firstname")),
                        "%" + filter.getFirstname().toLowerCase() + "%"
                ));
            }

            if (filter.getLastname() != null && !filter.getLastname().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("lastname")),
                        "%" + filter.getLastname().toLowerCase() + "%"
                ));
            }

            if (filter.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), filter.getRole()));
            }

            if (filter.getBlocked() != null) {
                predicates.add(cb.equal(root.get("blocked"), filter.getBlocked()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}