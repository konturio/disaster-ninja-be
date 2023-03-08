package io.kontur.disasterninja.dto.layer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.wololo.geojson.GeoJSON;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @param urls     for 'vector' and 'raster' only
 * @param tileSize for 'vector' and 'raster' only
 * @param data     for geoJson only
 */
public record LayerSourceDto(LayerSourceType type, List<String> urls, Integer tileSize, GeoJSON data, String apiKey) {

    public static LayerSourceDto fromLayer(Layer layer) {
        LayerSource layerSource = layer.getSource();
        if (layerSource == null) {
            return null;
        }
        return new LayerSourceDto(layerSource.getType(), defineUrlsForRequest(layerSource.getUrls(),
                layer.getProperties()), layerSource.getTileSize(), layerSource.getData(), layerSource.getApiKey());
    }

    private static List<String> defineUrlsForRequest(List<String> urls, ObjectNode layerProperties) {
        List<String> supportedLanguages = getSupportedLanguages(layerProperties);
        // Languages are currently used only for basemap layers
        if (CollectionUtils.isEmpty(supportedLanguages)) {
            return urls != null ? List.copyOf(urls) : null;
        }

        if (urls == null || urls.isEmpty()) {
            return null;
        }

        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            return null;
        }

        String userLanguage = request.getHeader("User-Language");
        if (supportedLanguages.contains(userLanguage)) {
            return List.of(urls.get(0).replace("{lang}", userLanguage));
        }

        if (supportedLanguages.contains("en")) {
            return List.of(urls.get(0).replace("{lang}", "en"));
        }

        return urls;
    }

    private static HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    private static List<String> getSupportedLanguages(ObjectNode layerProperties) {
        if (layerProperties == null || layerProperties.isNull() || layerProperties.isEmpty()) {
            return Collections.emptyList();
        }
        JsonNode langNode = layerProperties.get("lang");
        if (langNode == null || langNode.isNull() || langNode.isEmpty() || !langNode.isArray()) {
            return Collections.emptyList();
        }
        List<String> supportedLanguages = new ArrayList<>(langNode.size());

        for (JsonNode jsonNode : langNode) {
            supportedLanguages.add(jsonNode.asText());
        }
        return supportedLanguages;
    }
}
