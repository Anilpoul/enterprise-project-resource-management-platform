package com.enterprise.platform.sprint.mapper;

import com.enterprise.platform.sprint.dto.response.SprintResponse;
import com.enterprise.platform.sprint.entity.Sprint;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SprintMapper {

    SprintResponse toResponse(Sprint sprint);
}
