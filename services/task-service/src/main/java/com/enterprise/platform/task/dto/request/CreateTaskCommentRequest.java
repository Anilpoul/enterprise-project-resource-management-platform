package com.enterprise.platform.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskCommentRequest {

    @NotBlank(message = "Comment text is required")
    private String comment;
}
