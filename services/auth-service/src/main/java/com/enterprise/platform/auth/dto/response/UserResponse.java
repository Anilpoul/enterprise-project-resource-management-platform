package com.enterprise.platform.auth.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class UserResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private Set<String> roles;

}