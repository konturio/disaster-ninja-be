package io.kontur.disasterninja.service.converter;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.dto.application.LayersApiAppDto;
import io.kontur.disasterninja.dto.application.UpsAppDto;
import io.kontur.disasterninja.dto.application.AppContextDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppContextDtoConverter {

    private static final Logger LOG = LoggerFactory.getLogger(AppContextDtoConverter.class);

    private final LayersApiClient layersApiClient;

    public AppContextDto convert(UpsAppDto upsAppDto, LayersApiAppDto layersApiAppDto) {
        AppContextDto appContextDto = new AppContextDto();
        appContextDto.setId(upsAppDto.getId());
        appContextDto.setName(upsAppDto.getName());
        appContextDto.setDescription(upsAppDto.getDescription());
        appContextDto.setOwnedByUser(upsAppDto.getOwnedByUser());
        appContextDto.setFeaturesConfig(upsAppDto.getFeaturesConfig());
        appContextDto.setSidebarIconUrl(upsAppDto.getSidebarIconUrl());
        appContextDto.setFaviconUrl(upsAppDto.getFaviconUrl());

        if (upsAppDto.isPublic() != layersApiAppDto.isPublic()) {
            LOG.error("Different visibility for application in services: isPublic={} for User Profile Service, " +
                    "isPublic={} for Layers API", upsAppDto.isPublic(), layersApiAppDto.isPublic());
            throw new WebApplicationException("Application with id=" + appContextDto.getId() + " not found",
                    HttpStatus.NOT_FOUND);
        }

        appContextDto.setShowAllPublicLayers(layersApiAppDto.isShowAllPublicLayers());
        appContextDto.setDefaultLayers(layersApiAppDto.getDefaultCollections()
                .stream()
                .map(collection -> {
                    Layer layer = layersApiClient.convertToLayer(collection);
                    Layer layerDetails = layersApiClient.convertToLayerDetails(null, collection,
                            layersApiAppDto.getId());

                    layer.setLegend(layerDetails.getLegend());
                    layer.setMinZoom(layerDetails.getMinZoom());
                    layer.setMaxZoom(layerDetails.getMaxZoom());
                    layer.setSource(layerDetails.getSource());

                    return layer;
                })
                .collect(Collectors.toList()));

        return appContextDto;
    }
}
