package com.enterprise.platform.project.specification;

import com.enterprise.platform.project.dto.request.ProjectSearchCriteria;
import com.enterprise.platform.project.entity.Project;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProjectSpecification {

    private ProjectSpecification() {
    }

    public static Specification<Project> withCriteria(UUID organizationId, ProjectSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Enforce tenant isolation
            predicates.add(cb.equal(root.get("organizationId"), organizationId));

            if (criteria != null) {
                if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                    String pattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("name")), pattern),
                            cb.like(cb.lower(root.get("projectKey")), pattern)
                    ));
                }

                if (criteria.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
                }

                if (criteria.getProjectType() != null) {
                    predicates.add(cb.equal(root.get("projectType"), criteria.getProjectType()));
                }

                if (criteria.getLeadUserId() != null) {
                    predicates.add(cb.equal(root.get("leadUserId"), criteria.getLeadUserId()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
