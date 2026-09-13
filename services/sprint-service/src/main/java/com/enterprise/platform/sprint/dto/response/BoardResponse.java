package com.enterprise.platform.sprint.dto.response;

import com.enterprise.platform.sprint.constants.enums.BoardType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponse {

    private UUID id;

    private UUID organizationId;

    private UUID projectId;

    private String name;

    private String description;

    private BoardType boardType;

    @Builder.Default
    private List<BoardColumnResponse> columns = new ArrayList<>();
}
