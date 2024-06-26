package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.dto.AppDto;
import io.kontur.disasterninja.dto.AssetDto;
import io.kontur.disasterninja.dto.FeatureDto;
import io.kontur.disasterninja.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final UserProfileClient userProfileClient;

    public AppDto getAppConfig(UUID appId, String domain) {
        AppDto appDto = null;
        if (appId != null) {
            appDto = userProfileClient.getApp(appId);
        } else {
            // TODO: one UPS call can be made instead of three in case requester domain is unknown.
            //  Corresponding UPS changes required
            if (domain != null) {
                appDto = userProfileClient.getApp(domain);
            }
            if (appDto == null) { // requester domain is unknown
                appId = UUID.fromString(Objects.requireNonNull(userProfileClient.getDefaultAppId().getBody()));
                appDto = userProfileClient.getApp(appId);
            } else {
                appId = appDto.getId();
            }
        }

        if (AuthenticationUtil.isUserAuthenticated()) {
            appDto.setUser(userProfileClient.getCurrentUser());
        }

        // TODO: remove this logic when feature configs from UPS are received within List<FeatureDto> features parameter
        List<FeatureDto> features = userProfileClient.getUserAppFeatures(appId);
        AppDto finalAppDto = appDto;
        features.forEach(feature -> feature.setConfiguration(finalAppDto.getFeaturesConfig().get(feature.getName())));
        appDto.setFeatures(features);
        appDto.setFeaturesConfig(null);

        return appDto;
    }

    public Optional<AssetDto> getAsset(UUID appId, String filename) {
        ResponseEntity<AssetDto> assetDto = userProfileClient.getAsset(appId, filename);
        if (assetDto != null && HttpStatus.OK == assetDto.getStatusCode() && assetDto.getBody() != null) {
            return Optional.of(assetDto.getBody());
        }
        return Optional.empty();
    }
}
