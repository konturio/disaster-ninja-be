package io.kontur.disasterninja.notifications.email;

import io.kontur.disasterninja.dto.EmailDto;
import io.kontur.disasterninja.dto.Partner;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import io.kontur.disasterninja.notifications.MessageFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static io.kontur.disasterninja.util.FormatUtil.formatNumber;
import static java.time.ZoneOffset.UTC;
import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

@Component
@ConditionalOnProperty(value = "notifications.email.enabled")
public class EmailMessageFormatter extends MessageFormatter {

    private final static Logger LOG = LoggerFactory.getLogger(EmailMessageFormatter.class);

    private final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm 'UTC'");

    private final TemplateEngine templateEngine;

    @Value("${notifications.konturUrlPattern}")
    private String konturUrlPattern;

    @Value("${notifications.feed}")
    private String feed;

    public EmailMessageFormatter(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public EmailDto format(EventApiEventDto event, Map<String, Object> urbanPopulationProperties,
                            Map<String, Double> analytics, List<Partner> partners) throws IOException {
        FeedEpisode lastEpisode = getLatestEpisode(event);
        return new EmailDto(
                createSubject(event, lastEpisode),
                generateTemplate("gg-email-template.txt", event, lastEpisode, urbanPopulationProperties, analytics, partners),
                generateTemplate("gg-email-template.html", event, lastEpisode, urbanPopulationProperties, analytics, partners));
    }

    private String createSubject(EventApiEventDto event, FeedEpisode lastEpisode) {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessageColorCode(event, lastEpisode, true));
        sb.append(event.getName());
        return sb.toString();
    }

    public String generateTemplate(String template, EventApiEventDto event, FeedEpisode lastEpisode,
                                   Map<String, Object> urbanPopulationProperties,
                                   Map<String, Double> analytics, List<Partner> partners) {
        Context context = new Context();
        context.setVariable("link", String.format(konturUrlPattern, event.getEventId(), feed));
        context.setVariable("description", lastEpisode.getDescription());
        context.setVariable("urbanPopulation", formatNumber(urbanPopulationProperties.get("population")));
        context.setVariable("urbanArea", formatNumber(urbanPopulationProperties.get("areaKm2")));
        context.setVariable("population", formatNumber(lastEpisode.getEpisodeDetails().get("population")));
        context.setVariable("populatedArea", formatNumber(lastEpisode.getEpisodeDetails().get("populatedAreaKm2")));
        context.setVariable("industrialArea", formatNumber(lastEpisode.getEpisodeDetails().get("industrialAreaKm2")));
        context.setVariable("forestArea", formatNumber(lastEpisode.getEpisodeDetails().get("forestAreaKm2")));
        context.setVariable("osmGapsArea", formatNumber(analytics.get("osmGapsArea")));
        context.setVariable("osmGapsPercentage", formatNumber(analytics.get("osmGapsPercentage")));
        context.setVariable("osmGapsPopulation", formatNumber(analytics.get("osmGapsPopulation")));
        context.setVariable("noBuildingsArea", formatNumber(analytics.get("noBuildingsArea")));
        context.setVariable("noBuildingsPopulation", formatNumber(analytics.get("noBuildingsPopulation")));
        context.setVariable("noRoadsArea", formatNumber(analytics.get("noRoadsArea")));
        context.setVariable("noRoadsPopulation", formatNumber(analytics.get("noRoadsPopulation")));
        context.setVariable("type", capitalizeFully(event.getType()));
        context.setVariable("severity", capitalizeFully(event.getSeverity().name()));
        context.setVariable("location", event.getLocation());
        context.setVariable("startedAt", event.getStartedAt().withOffsetSameInstant(UTC).format(dateFormatter));
        context.setVariable("updatedAt", event.getUpdatedAt().withOffsetSameInstant(UTC).format(dateFormatter));
        context.setVariable("partners", partners);

        return templateEngine.process(template, context);
    }
}
