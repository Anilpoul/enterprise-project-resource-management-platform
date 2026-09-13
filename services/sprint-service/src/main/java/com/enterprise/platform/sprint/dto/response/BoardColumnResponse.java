package com.enterprise.platform.sprint.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardColumnResponse {

    private UUID id;

    private UUID boardId;

    private String name;

    private String taskStatus;

    private Integer columnOrder;

    private Integer wipLimit;
}
