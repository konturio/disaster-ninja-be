package io.kontur.disasterninja.dto.layer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Data
public class LayerSummaryDto {

    private final String id;
    private final String name;
    private final String description;
    private final LayerCategory category;
    private final String group;
    private final boolean boundaryRequiredForRetrieval;
    private final boolean eventIdRequiredForRetrieval;
    private final List<String> copyrights;
    private final boolean ownedByUser;
    private final ObjectNode featureProperties;
    private final String mapboxStyle;

    public static LayerSummaryDto fromLayer(Layer layer) {
        return layer == null ? null : new LayerSummaryDto(layer.getId(), layer.getName(),
                layer.getDescription(), layer.getCategory() == null ? null : layer.getCategory(),
                layer.getGroup(), layer.isBoundaryRequiredForRetrieval(), layer.isEventIdRequiredForRetrieval(),
                layer.getCopyrights(), layer.isOwnedByUser(), layer.getFeatureProperties(), defineMapboxStyleForRequest(layer));
    }

    private static String defineMapboxStyleForRequest(Layer layer) {
        if (layer.getMapboxStyles() == null || layer.getMapboxStyles().isNull() || layer.getMapboxStyles().isEmpty()) {
            return null;
        }

        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            return null;
        }

        String url = getMapboxUrl(layer);
        if (isBlank(url)) {
            return null;
        }

        List<String> supportedLanguages = getSupportedLanguages(layer);
        if (CollectionUtils.isEmpty(supportedLanguages)) {
            return url;
        }

        String userLanguage = request.getHeader("User-Language");
        if (supportedLanguages.contains(userLanguage)) {
            return url.replace("{lang}", userLanguage);
        }

        if (supportedLanguages.contains("en")) {
            return url.replace("{lang}", "en");
        }

        return url;
    }

    private static HttpServletRequest getCurrentHttpRequest(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes)requestAttributes).getRequest();
        }
        return null;
    }

    private static String getMapboxUrl(Layer layer) {
        JsonNode urlNode = layer.getMapboxStyles().get("url");
        if (urlNode == null || !urlNode.isTextual()) {
            return null;
        }
        return urlNode.asText();
    }

    private static List<String> getSupportedLanguages(Layer layer) {
        JsonNode langNode = layer.getMapboxStyles().get("lang");
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
