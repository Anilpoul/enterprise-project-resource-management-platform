package com.enterprise.platform.user.service.impl;

import com.enterprise.platform.user.service.EmployeeCodeGenerator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSequenceEmployeeCodeGenerator
        implements EmployeeCodeGenerator {

    private final EntityManager entityManager;

    @Override
    public String generate() {

        Long nextValue =
                ((Number) entityManager
                        .createNativeQuery(
                                "SELECT nextval('employee_code_seq')"
                        )
                        .getSingleResult())
                        .longValue();

        return String.format(
                "EMP%06d",
                nextValue
        );
    }
}