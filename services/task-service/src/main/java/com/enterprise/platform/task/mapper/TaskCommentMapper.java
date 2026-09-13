package com.enterprise.platform.task.mapper;

import com.enterprise.platform.task.dto.response.TaskCommentResponse;
import com.enterprise.platform.task.entity.TaskComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskCommentMapper {

    @Mapping(target = "taskId", source = "task.id")
    TaskCommentResponse toResponse(TaskComment comment);
}
