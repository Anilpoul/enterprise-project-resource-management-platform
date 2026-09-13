package com.enterprise.platform.notification.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferenceRequest {

    private Boolean emailEnabled;

    private Boolean inAppEnabled;

    private Boolean taskNotifications;

    private Boolean sprintNotifications;

    private Boolean resourceNotifications;
}
