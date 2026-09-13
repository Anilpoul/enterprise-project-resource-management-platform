package com.enterprise.platform.notification.mapper;

import com.enterprise.platform.notification.dto.request.UpdatePreferenceRequest;
import com.enterprise.platform.notification.dto.response.NotificationPreferenceResponse;
import com.enterprise.platform.notification.entity.NotificationPreference;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationPreferenceMapper {

    NotificationPreferenceResponse toResponse(NotificationPreference preference);

    void updateEntityFromRequest(UpdatePreferenceRequest request, @MappingTarget NotificationPreference preference);
}
