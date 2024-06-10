package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.AppDto;
import io.kontur.disasterninja.dto.AppSummaryDto;
import io.kontur.disasterninja.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * Checks if user has access to any of the provided features
     * @param featureNames feature names to check user access
     * @return true if user has access to at least one of the provided features,
     *         false otherwise
     */
    public boolean userHasAccessToFeature(List<String> featureNames) {
        List<AppSummaryDto> appSummaries = client.getAppsList();
        for (AppSummaryDto appSummary : appSummaries) {
            AppDto app = client.getApp(appSummary.getId());
            if (app.getFeatures().stream().anyMatch(feature -> featureNames.contains(feature.getName()))) {
                return true;
            }
        }
        return false;
    }
}
