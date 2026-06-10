package com.enterprise.platform.user.service;

import com.enterprise.platform.events.AuthEvent;

public interface UserProfileService {

    void createUserProfile(AuthEvent event);
}
