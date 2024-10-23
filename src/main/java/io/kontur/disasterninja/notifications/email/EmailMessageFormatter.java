package io.kontur.disasterninja.notifications.email;

import io.kontur.disasterninja.dto.EmailDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import io.kontur.disasterninja.notifications.MessageFormatter;
import io.micrometer.core.instrument.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static io.kontur.disasterninja.util.FormatUtil.formatNumber;
import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

@Component
@ConditionalOnProperty(value = "notifications.enabled")
public class EmailMessageFormatter extends MessageFormatter {

    private final static Logger LOG = LoggerFactory.getLogger(EmailMessageFormatter.class);

    private final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @Value("${notifications.konturUrlPattern}")
    private String konturUrlPattern;

    @Value("${notifications.feed}")
    private String feed;

    public EmailDto format(EventApiEventDto event, Map<String, Object> urbanPopulationProperties,
                            Map<String, Double> analytics) throws IOException {
        String textTemplate = loadTemplate("notification/gg-email-template.txt");
        String htmlTemplate = loadTemplate("notification/gg-email-template.html");

        FeedEpisode lastEpisode = getLatestEpisode(event);

        return new EmailDto(
                createSubject(event, lastEpisode),
                replacePlaceholders(textTemplate, event, lastEpisode, urbanPopulationProperties),
                replacePlaceholders(htmlTemplate, event, lastEpisode, urbanPopulationProperties));
    }

    private String loadTemplate(String templateName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templateName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Failed to find notification template: " + templateName);
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }

    private String createSubject(EventApiEventDto event, FeedEpisode lastEpisode) {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessageColorCode(event, lastEpisode, true));
        sb.append(getEventStatus(event));
        sb.append(event.getName());
        return sb.toString();
    }

    private String replacePlaceholders(String template, EventApiEventDto event, FeedEpisode lastEpisode, Map<String, Object> urbanPopulationProperties) {

        return template
                .replace("${name}", event.getName())
                .replace("${link}", String.format(konturUrlPattern, event.getEventId(), feed))
                .replace("${description}", lastEpisode.getDescription())
                .replace("${urbanPopulation}", formatNumber(urbanPopulationProperties.get("population")))
                .replace("${urbanArea}", formatNumber(urbanPopulationProperties.get("areaKm2")))
                .replace("${population}", formatNumber(lastEpisode.getEpisodeDetails().get("population")))
                .replace("${populatedArea}", formatNumber(lastEpisode.getEpisodeDetails().get("populatedAreaKm2")))
                .replace("${industrialArea}", formatNumber(lastEpisode.getEpisodeDetails().get("industrialAreaKm2")))
                .replace("${forestArea}", formatNumber(lastEpisode.getEpisodeDetails().get("forestAreaKm2")))
                .replace("${location}", event.getLocation())
                .replace("${type}", capitalizeFully(event.getType()))
                .replace("${severity}", capitalizeFully(event.getSeverity().name()))
                .replace("${startedAt}", event.getStartedAt().format(dateFormatter))
                .replace("${updatedAt}", event.getUpdatedAt().format(dateFormatter));
    }

}
