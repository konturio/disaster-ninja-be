package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.ActiveSubscriptionDto;
import io.kontur.disasterninja.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileService.class);
    private final UserProfileClient client;

    public String getUserDefaultFeed() {
        try {
            return client.getUserDefaultFeed();
        } catch (Exception e) {
            LOG.error("Can't get default user event feed: {}", e.getMessage(), e);
            throw new WebApplicationException("Can't get default user event feed", HttpStatus.BAD_GATEWAY);
        }
    }

    public UserDto getCurrentUser() {
        try {
            return client.getCurrentUser();
        } catch (Exception e) {
            LOG.error("Can't get current user: {}", e.getMessage(), e);
            throw new WebApplicationException("Can't get current user", HttpStatus.BAD_GATEWAY);
        }
    }

    public UserDto updateUser(UserDto userDto) {
        try {
            return client.updateUser(userDto);
        } catch (Exception e) {
            LOG.error("Can't update current user: {}", e.getMessage(), e);
            throw new WebApplicationException("Can't update current user", HttpStatus.BAD_GATEWAY);
        }
    }

    public Optional<ActiveSubscriptionDto> getActiveSubscription(UUID appId) {
        return client.getActiveSubscription(appId);
    }

    public ActiveSubscriptionDto setActiveSubscription(UUID appId, String billingPlanId, String billingSubscriptionId) {
        return client.setActiveSubscription(appId, billingPlanId, billingSubscriptionId);
    }
}
