package com.enterprise.platform.user.service.impl;

import com.enterprise.platform.user.constants.enums.UserProfileStatus;
import com.enterprise.platform.user.entity.UserProfile;
import com.enterprise.platform.events.AuthEvent;
import com.enterprise.platform.user.repository.UserProfileRepository;
import com.enterprise.platform.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl
        implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public void createUserProfile(AuthEvent event) {

        if (userProfileRepository
                .findByUserId(event.getUserId())
                .isPresent()) {

            return;
        }

        UserProfile profile = new UserProfile();

        profile.setUserId(event.getUserId());

        profile.setFirstName(event.getFirstName());

        profile.setLastName(event.getLastName());

        profile.setEmail(event.getEmail());

        profile.setStatus(UserProfileStatus.ACTIVE);

        userProfileRepository.save(profile);
    }
}
