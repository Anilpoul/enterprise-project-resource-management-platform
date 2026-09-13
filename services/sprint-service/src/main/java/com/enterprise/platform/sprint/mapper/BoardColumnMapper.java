package com.enterprise.platform.sprint.mapper;

import com.enterprise.platform.sprint.dto.response.BoardColumnResponse;
import com.enterprise.platform.sprint.entity.BoardColumn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BoardColumnMapper {

    @Mapping(target = "boardId", source = "board.id")
    BoardColumnResponse toResponse(BoardColumn column);
}
