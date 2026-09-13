package com.enterprise.platform.sprint.mapper;

import com.enterprise.platform.sprint.dto.response.BoardResponse;
import com.enterprise.platform.sprint.entity.Board;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {BoardColumnMapper.class})
public interface BoardMapper {

    BoardResponse toResponse(Board board);
}
