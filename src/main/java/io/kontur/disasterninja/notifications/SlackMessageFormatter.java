package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class SlackMessageFormatter {

    private static final String BODY = "{\"text\":\"><%s|%s>%s\"}";
    private static final String gdacsReportLinkPattern = "https://www.gdacs.org/report.aspx?eventtype=%s&eventid=%s";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");
    @Value("${notifications.alertUrlPattern:}")
    private String notificationAlertUrlPattern;

    public String format(EventApiEventDto event, Map<String, Object> urbanPopulationProperties) {

        List<FeedEpisode> eventEpisodes = event.getEpisodes();
        eventEpisodes.sort(Comparator.comparing(FeedEpisode::getUpdatedAt));

        FeedEpisode latestEpisode = eventEpisodes.get(eventEpisodes.size() - 1);
        String alertUrl = createAlertLink(event, latestEpisode);
        StringBuilder description = new StringBuilder();
        if (StringUtils.isNoneBlank(latestEpisode.getDescription())) {
            description.append("\\n>").append(latestEpisode.getDescription());
        }
        Map<String, Object> episodeDetails = latestEpisode.getEpisodeDetails();
        Map<String, Object> eventDetails = event.getEventDetails();

        description.append(convertUrbanStatistic(urbanPopulationProperties));
        description.append(convertPopulationStatistic(episodeDetails));
        description.append(convertIndustrialStatistic(episodeDetails, eventDetails));
        description.append(convertForestStatistic(episodeDetails, eventDetails));
        description.append(convertFireStatistic(episodeDetails, eventDetails, latestEpisode));

        return String.format(BODY, alertUrl, event.getName(), description);
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

    private String formatNumber(Object number) {
        if (number == null) {
            return "0";
        }
        return DECIMAL_FORMAT.format(new BigDecimal(String.valueOf(number)));
    }

}
