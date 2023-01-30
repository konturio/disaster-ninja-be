package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.dto.AppDto;
import io.kontur.disasterninja.dto.FeatureDto;
import io.kontur.disasterninja.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final UserProfileClient userProfileClient;

    public AppDto getAppConfig(UUID appId) {
        if (appId == null) {
            appId = UUID.fromString(Objects.requireNonNull(userProfileClient.getDefaultAppId().getBody()));
        }
        AppDto appDto = userProfileClient.getApp(appId);
        if (AuthenticationUtil.isUserAuthenticated()) {
            appDto.setUser(userProfileClient.getCurrentUser());
        }

        // TODO: remove this logic when feature configs from UPS are received within List<FeatureDto> features parameter
        List<FeatureDto> features = userProfileClient.getUserAppFeatures(appId);
        features.forEach(feature -> feature.setConfiguration(appDto.getFeaturesConfig().get(feature.getName())));
        appDto.setFeatures(features);
        appDto.setFeaturesConfig(null);

        return appDto;
    }
}
