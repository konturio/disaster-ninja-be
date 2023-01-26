package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.client.UserProfileClient;
import io.kontur.disasterninja.dto.application.LayersApiAppDto;
import io.kontur.disasterninja.dto.application.UpsAppDto;
import io.kontur.disasterninja.dto.application.AppContextDto;
import io.kontur.disasterninja.service.converter.AppContextDtoConverter;
import io.kontur.disasterninja.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final LayersApiClient layersApiClient;
    private final UserProfileClient userProfileClient;
    private final EventApiClient eventApiClient;
    private final AppContextDtoConverter appContextDtoConverter;

    public AppContextDto getAppContext(UUID appId) {
        if (appId == null) {
            appId = UUID.fromString(Objects.requireNonNull(userProfileClient.getDefaultAppId().getBody()));
        }
        UpsAppDto upsAppDto = userProfileClient.getApp(appId);
        LayersApiAppDto layersApiAppDto = layersApiClient.getApp(appId);
        AppContextDto appContextDto = appContextDtoConverter.convert(upsAppDto, layersApiAppDto);
        if (AuthenticationUtil.isUserAuthenticated()) {
            appContextDto.setUser(userProfileClient.getCurrentUser());
        }
        appContextDto.setUserFeeds(eventApiClient.getUserFeeds());

        return appContextDto;
    }
}
