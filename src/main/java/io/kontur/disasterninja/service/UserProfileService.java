package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
}
