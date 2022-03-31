package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.dto.Severity;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

@Component
@ConditionalOnProperty(value="notifications.enabled")
public class SlackMessageFormatter {

    private static final String BODY = "{\"text\":\"><%s|%s>%s\"}";
    private static final String gdacsReportLinkPattern = "https://www.gdacs.org/report.aspx?eventtype=%s&eventid=%s";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##",
            new DecimalFormatSymbols(Locale.US));
    @Value("${notifications.alertUrlPattern:}")
    private String notificationAlertUrlPattern;

    public String format(EventApiEventDto event, Map<String, Object> urbanPopulationProperties,
                         Map<String, Double> analytics) {

        List<FeedEpisode> eventEpisodes = event.getEpisodes();
        eventEpisodes.sort(Comparator.comparing(FeedEpisode::getUpdatedAt));

        FeedEpisode latestEpisode = eventEpisodes.get(eventEpisodes.size() - 1);
        Map<String, Object> episodeDetails = latestEpisode.getEpisodeDetails();
        Map<String, Object> eventDetails = event.getEventDetails();

        StringBuilder description = new StringBuilder();
        description.append(convertNotificationDescription(latestEpisode));
        description.append(convertUrbanStatistic(urbanPopulationProperties));
        description.append(convertPopulationStatistic(episodeDetails));
        description.append(convertIndustrialStatistic(episodeDetails, eventDetails));
        description.append(convertForestStatistic(episodeDetails, eventDetails));
        description.append(convertFireStatistic(episodeDetails, eventDetails, latestEpisode));
        description.append(convertOsmQuality(analytics));

        String colorCode = getMessageColorCode(event, latestEpisode);
        String status = getEventStatus(event);
        String alertUrl = createAlertLink(event, latestEpisode);
        String title = colorCode + status + event.getName();
        return String.format(BODY, alertUrl, title, description);
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
        String pattern = "\\n>:family-div: Total population: %s people on %s km².";
        String population = formatNumber(episodeDetails.get("population"));
        String area = formatNumber(episodeDetails.get("populatedAreaKm2"));
        if ("0".equals(population) && "0".equals(area)) {
            return "";
        }
        return String.format(pattern, population, area);
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
                                        FeedEpisode latestEpisode) {
        if (!"WILDFIRE".equals(latestEpisode.getType())) {
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

        result.append(convertOsmGapsPercentage(analytics));
        result.append(convertOsmGapsValues(analytics));
        result.append(convertNoBuildingsValues(analytics));
        result.append(convertNoRoadsValue(analytics));

        if (StringUtils.isNotBlank(result)) {
            return "\\n>OSM quality:" + result;
        }
        return "";
    }

    private String convertOsmGapsPercentage(Map<String, Double> analytics) {
        String osmGaps = "\\n>:mag_right: OSM gaps: %s%%.";
        String osmGapsPercentage = formatNumber(analytics.get("osmGapsPercentage"));
        if (!"0".equals(osmGapsPercentage)) {
            return String.format(osmGaps, osmGapsPercentage);
        }
        return "";
    }

    private String convertOsmGapsValues(Map<String, Double> analytics) {
        String osmObjects = "\\n>:world_map: Populated area without OSM objects: %s km², contains %s people.";
        String osmGapsArea = formatNumber(analytics.get("osmGapsArea"));
        String osmGapsPopulation = formatNumber(analytics.get("osmGapsPopulation"));
        if (!"0".equals(osmGapsArea) && !"0".equals(osmGapsPopulation)) {
            return String.format(osmObjects, osmGapsArea, osmGapsPopulation);
        }
        return "";
    }

    private String convertNoBuildingsValues(Map<String, Double> analytics) {
        String osmBuildings = "\\n>:house_buildings: Populated area without OSM buildings: %s km², contains %s people.";
        String noBuildingsArea = formatNumber(analytics.get("noBuildingsArea"));
        String noBuildingsPopulation = formatNumber(analytics.get("noBuildingsPopulation"));
        if (!"0".equals(noBuildingsArea) && !"0".equals(noBuildingsPopulation)) {
            return String.format(osmBuildings, noBuildingsArea, noBuildingsPopulation);
        }
        return "";
    }

    private String convertNoRoadsValue(Map<String, Double> analytics) {
        String osmRoads = "\\n>:motorway: Populated area without OSM roads: %s km², contains %s people.";
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

    private String getEventStatus(EventApiEventDto event) {
        return event.getVersion() == 1 ? "" : "[Update] ";
    }

    private String getMessageColorCode(EventApiEventDto event, FeedEpisode latestEpisode) {
        String name = event.getName();

        if (StringUtils.isNotBlank(name) && name.startsWith("Green")) {
            return ":large_green_circle: ";
        } else if (StringUtils.isNotBlank(name) && name.startsWith("Orange")) {
            return ":large_orange_circle: ";
        } else if (StringUtils.isNotBlank(name) && name.startsWith("Red")) {
            return ":red_circle: ";
        } else if (Severity.TERMINATION.equals(latestEpisode.getSeverity())) {
            return "▰▱▱▱▱ ";
        } else if (Severity.MINOR.equals(latestEpisode.getSeverity())) {
            return "▰▰▱▱▱ ";
        } else if (Severity.MODERATE.equals(latestEpisode.getSeverity())) {
            return "▰▰▰▱▱ ";
        } else if (Severity.SEVERE.equals(latestEpisode.getSeverity())) {
            return "▰▰▰▰▱ ";
        } else if (Severity.EXTREME.equals(latestEpisode.getSeverity())) {
            return "▰▰▰▰▰ ";
        }
        return "";
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

    private String formatNumber(Object number) {
        if (number == null) {
            return "0";
        }
        return DECIMAL_FORMAT.format(new BigDecimal(String.valueOf(number)));
    }

}
