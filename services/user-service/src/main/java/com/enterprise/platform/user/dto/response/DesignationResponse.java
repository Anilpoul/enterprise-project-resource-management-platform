package com.enterprise.platform.user.dto.response;

import com.enterprise.platform.user.constants.enums.DesignationStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DesignationResponse {

    private UUID id;

    private String name;

    private String description;

    private Integer level;

    private DesignationStatus status;
}