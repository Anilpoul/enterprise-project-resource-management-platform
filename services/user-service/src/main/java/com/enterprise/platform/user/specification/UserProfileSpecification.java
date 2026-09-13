package com.enterprise.platform.user.specification;

import com.enterprise.platform.user.dto.request.UserSearchRequest;
import com.enterprise.platform.user.entity.UserProfile;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserProfileSpecification {

    private UserProfileSpecification() {
    }

    public static Specification<UserProfile> search(
            UserSearchRequest request
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            if (request.getKeyword() != null
                    && !request.getKeyword().isBlank()) {

                String keyword =
                        "%" +
                                request.getKeyword()
                                        .trim()
                                        .toLowerCase() +
                                "%";

                predicates.add(

                        cb.or(

                                cb.like(
                                        cb.lower(
                                                root.get(
                                                        "firstName"
                                                )
                                        ),
                                        keyword
                                ),

                                cb.like(
                                        cb.lower(
                                                root.get(
                                                        "lastName"
                                                )
                                        ),
                                        keyword
                                ),

                                cb.like(
                                        cb.lower(
                                                root.get(
                                                        "email"
                                                )
                                        ),
                                        keyword
                                )
                        )
                );
            }

            if (request.getDepartmentId()
                    != null) {

                predicates.add(

                        cb.equal(
                                root.get(
                                        "department"
                                ).get(
                                        "id"
                                ),
                                request.getDepartmentId()
                        )
                );
            }

            if (request.getDesignationId()
                    != null) {

                predicates.add(

                        cb.equal(
                                root.get(
                                        "designation"
                                ).get(
                                        "id"
                                ),
                                request.getDesignationId()
                        )
                );
            }

            if (request.getManagerId()
                    != null) {

                predicates.add(

                        cb.equal(
                                root.get(
                                        "managerId"
                                ),
                                request.getManagerId()
                        )
                );
            }

            if (request.getStatus()
                    != null) {

                predicates.add(

                        cb.equal(
                                root.get(
                                        "status"
                                ),
                                request.getStatus()
                        )
                );
            }

            if (request.getEmployeeCode()
                    != null
                    && !request.getEmployeeCode()
                    .isBlank()) {

                predicates.add(

                        cb.equal(
                                root.get(
                                        "employeeCode"
                                ),
                                request.getEmployeeCode()
                        )
                );
            }

            if (request.getOrganizationId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("organizationId"),
                                request.getOrganizationId()
                        )
                );
            }

            return cb.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }
}