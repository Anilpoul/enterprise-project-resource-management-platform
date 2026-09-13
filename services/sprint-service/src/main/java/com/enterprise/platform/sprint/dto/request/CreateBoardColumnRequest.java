package com.enterprise.platform.sprint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBoardColumnRequest {

    @NotBlank(message = "Column name is required")
    private String name;

    @NotBlank(message = "Task status is required")
    private String taskStatus;

    @NotNull(message = "Column order is required")
    private Integer columnOrder;

    private Integer wipLimit;
}
