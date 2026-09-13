package com.enterprise.platform.notification.mapper;

import com.enterprise.platform.notification.dto.request.CreateNotificationRequest;
import com.enterprise.platform.notification.dto.response.NotificationResponse;
import com.enterprise.platform.notification.entity.Notification;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "readAt", ignore = true)
    Notification toEntity(CreateNotificationRequest request);
}
