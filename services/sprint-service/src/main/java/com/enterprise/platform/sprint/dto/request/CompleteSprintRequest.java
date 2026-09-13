package com.enterprise.platform.sprint.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteSprintRequest {

    private Integer completedStoryPoints;

    private Integer totalStoryPoints;
}
