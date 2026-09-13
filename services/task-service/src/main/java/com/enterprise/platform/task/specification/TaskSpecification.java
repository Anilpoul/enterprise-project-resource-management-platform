package com.enterprise.platform.task.specification;

import com.enterprise.platform.task.dto.request.TaskSearchCriteria;
import com.enterprise.platform.task.entity.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> buildSpecification(UUID organizationId, TaskSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("organizationId"), organizationId));

            if (criteria != null) {
                if (criteria.getProjectId() != null) {
                    predicates.add(cb.equal(root.get("projectId"), criteria.getProjectId()));
                }
                if (criteria.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
                }
                if (criteria.getTaskType() != null) {
                    predicates.add(cb.equal(root.get("taskType"), criteria.getTaskType()));
                }
                if (criteria.getPriority() != null) {
                    predicates.add(cb.equal(root.get("priority"), criteria.getPriority()));
                }
                if (criteria.getAssigneeId() != null) {
                    predicates.add(cb.equal(root.get("assigneeId"), criteria.getAssigneeId()));
                }
                if (criteria.getReporterId() != null) {
                    predicates.add(cb.equal(root.get("reporterId"), criteria.getReporterId()));
                }
                if (criteria.getSprintId() != null) {
                    predicates.add(cb.equal(root.get("sprintId"), criteria.getSprintId()));
                }
                if (criteria.getParentTaskId() != null) {
                    predicates.add(cb.equal(root.get("parentTaskId"), criteria.getParentTaskId()));
                }
                if (criteria.getSearch() != null && !criteria.getSearch().isBlank()) {
                    String pattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                    Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                    Predicate keyMatch = cb.like(cb.lower(root.get("taskKey")), pattern);
                    predicates.add(cb.or(titleMatch, keyMatch));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
