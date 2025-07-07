package io.kontur.disasterninja.notifications.slack;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import io.kontur.disasterninja.notifications.MessageFormatter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.kontur.disasterninja.util.FormatUtil.formatNumber;

@Component
@ConditionalOnProperty(value = "notifications.slack.enabled")
public class SlackMessageFormatter extends MessageFormatter {

    private static final String BODY = "{\"text\":\"><%s|%s>%s\", \"unfurl_links\":true, \"unfurl_media\": true}";
    private static final String gdacsReportLinkPattern = "https://www.gdacs.org/report.aspx?eventtype=%s&eventid=%s";

    @Value("${notifications.alertUrlPattern:}")
    private String notificationAlertUrlPattern;

    public String format(EventApiEventDto event, Map<String, Object> urbanPopulationProperties,
                         Map<String, Double> analytics) {
        FeedEpisode latestEpisode = getLatestEpisode(event);
        Map<String, Object> episodeDetails = latestEpisode.getEpisodeDetails();
        Map<String, Object> eventDetails = event.getEventDetails();

        StringBuilder description = new StringBuilder();
        description.append(convertNotificationDescription(latestEpisode));
        description.append(convertUrbanStatistic(urbanPopulationProperties));
        description.append(convertPopulationStatistic(episodeDetails));
        description.append(convertIndustrialStatistic(episodeDetails, eventDetails));
        description.append(convertForestStatistic(episodeDetails, eventDetails));
        description.append(convertFireStatistic(episodeDetails, eventDetails, event));
        description.append(convertOsmQuality(analytics));

        String colorCode = getMessageColorCode(event, latestEpisode, false);
        String status = getEventStatus(event);
        String alertUrl = createAlertLink(event, latestEpisode);
        String title = colorCode + status + sanitizeEventName(event.getName());
        return String.format(BODY, alertUrl, title, description);
    }

    static String sanitizeEventName(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        return name.replace(">=", "\u2265");
    }

    private String convertNotificationDescription(FeedEpisode latestEpisode) {
        String description = latestEpisode.getDescription();
        if (StringUtils.isBlank(description)) {
            return "";
        }
        return "\\n>" + description;
    }

    private String convertUrbanStatistic(Map<String, Object> urbanPopulationProperties) {
        if (urbanPopulationProperties == null) {
            return "";
        }
        String pattern = "\\n>:cityscape: Urban core: %s people on %s km².";
        String population = formatNumber(urbanPopulationProperties.get("population"));
        String area = formatNumber(urbanPopulationProperties.get("areaKm2"));
        if ("0".equals(population) && "0".equals(area)) {
            return "";
        }
        return String.format(pattern, population, area);
    }

    private String convertPopulationStatistic(Map<String, Object> episodeDetails) {
        String population = formatNumber(episodeDetails.get("population"));
        String area = formatNumber(episodeDetails.get("populatedAreaKm2"));
        if ("0".equals(population) && "0".equals(area)) {
            return "";
        }
        if (!"0".equals(population) && "0".equals(area)) {
            return String.format("\\n>:family-div: Total population: %s people.", population);
        }
        return String.format("\\n>:family-div: Total population: %s people on %s km².", population, area);
    }

    private String convertIndustrialStatistic(Map<String, Object> episodeDetails, Map<String, Object> eventDetails) {
        String patternEpisode = "\\n>:factory: Industrial area: %s km²";
        String patternEvent = " (currently), %s km² (from beginning)";
        String episodeValue = formatNumber(episodeDetails.get("industrialAreaKm2"));
        String eventValue = formatNumber(eventDetails.get("industrialAreaKm2"));
        return convertStatistic(patternEpisode, patternEvent, episodeValue, eventValue);
    }

    private String convertForestStatistic(Map<String, Object> episodeDetails, Map<String, Object> eventDetails) {
        String patternEpisode = "\\n>:deciduous_tree: Forest area: %s km²";
        String patternEvent = " (currently), %s km² (from beginning)";
        String episodeValue = formatNumber(episodeDetails.get("forestAreaKm2"));
        String eventValue = formatNumber(eventDetails.get("forestAreaKm2"));
        return convertStatistic(patternEpisode, patternEvent, episodeValue, eventValue);
    }

    private String convertFireStatistic(Map<String, Object> episodeDetails, Map<String, Object> eventDetails,
                                        EventApiEventDto event) {
        if (!"WILDFIRE".equals(event.getType())) {
            return "";
        }
        String patternEpisode = "\\n>:sparkles: Wildfire days in last year: %s";
        String patternEvent = " (episode), %s (event)";
        String episodeValue = formatNumber(episodeDetails.get("hotspotDaysPerYearMax"));
        String eventValue = formatNumber(eventDetails.get("hotspotDaysPerYearMax"));
        return convertStatistic(patternEpisode, patternEvent, episodeValue, eventValue);
    }

    private String convertOsmQuality(Map<String, Double> analytics) {
        StringBuilder result = new StringBuilder();

        result.append(convertOsmGapsValues(analytics));
        result.append(convertNoBuildingsValues(analytics));
        result.append(convertNoRoadsValue(analytics));

        if (StringUtils.isNotBlank(result)) {
            return "\\n>OpenStreetMap gaps:" + result;
        }
        return "";
    }

    private String convertOsmGapsValues(Map<String, Double> analytics) {
        String osmObjects = "\\n>:world_map: %s km² (%s%%) of populated area needs a map for %s people.";
        String osmGapsArea = formatNumber(analytics.get("osmGapsArea"));
        String osmGapsPercentage = formatNumber(analytics.get("osmGapsPercentage"));
        String osmGapsPopulation = formatNumber(analytics.get("osmGapsPopulation"));
        if (!"0".equals(osmGapsArea) && !"0".equals(osmGapsPopulation)) {
            return String.format(osmObjects, osmGapsArea, osmGapsPercentage, osmGapsPopulation);
        }
        return "";
    }

    private String convertNoBuildingsValues(Map<String, Double> analytics) {
        String osmBuildings = "\\n>:house_buildings: Buildings map gaps: %s km² for %s people.";
        String noBuildingsArea = formatNumber(analytics.get("noBuildingsArea"));
        String noBuildingsPopulation = formatNumber(analytics.get("noBuildingsPopulation"));
        if (!"0".equals(noBuildingsArea) && !"0".equals(noBuildingsPopulation)) {
            return String.format(osmBuildings, noBuildingsArea, noBuildingsPopulation);
        }
        return "";
    }

    private String convertNoRoadsValue(Map<String, Double> analytics) {
        String osmRoads = "\\n>:motorway: Roads map gaps: %s km² for %s people.";
        String noRoadsArea = formatNumber(analytics.get("noRoadsArea"));
        String noRoadsPopulation = formatNumber(analytics.get("noRoadsPopulation"));
        if (!"0".equals(noRoadsArea) && !"0".equals(noRoadsPopulation)) {
            return String.format(osmRoads, noRoadsArea, noRoadsPopulation);
        }
        return "";
    }

    private String convertStatistic(String patternEpisode, String patternEvent, String episodeValue,
                                    String eventValue) {
        if ("0".equals(episodeValue) && "0".equals(eventValue)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        if (episodeValue.equals(eventValue)) {
            result.append(String.format(patternEpisode, episodeValue));
        } else {
            result.append(String.format(patternEpisode, episodeValue))
                    .append(String.format(patternEvent, eventValue));
        }
        result.append(".");
        return result.toString();
    }

    private String createAlertLink(EventApiEventDto event, FeedEpisode episode) {
        if (StringUtils.isBlank(notificationAlertUrlPattern)) {
            Map<String, Object> properties = episode.getGeometries().getFeatures()[0].getProperties();
            String eventId = String.valueOf(properties.get("eventid"));
            String type = String.valueOf(properties.get("eventtype"));
            return String.format(gdacsReportLinkPattern, type, eventId);
        } else {
            return String.format(notificationAlertUrlPattern, event.getEventId().toString());
        }
    }
}
