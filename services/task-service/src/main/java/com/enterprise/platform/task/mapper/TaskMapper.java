package com.enterprise.platform.task.mapper;

import com.enterprise.platform.task.dto.response.TaskResponse;
import com.enterprise.platform.task.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "commentCount", expression = "java(task.getComments() != null ? task.getComments().size() : 0)")
    TaskResponse toResponse(Task task);
}
