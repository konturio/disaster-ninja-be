package io.kontur.disasterninja.notifications.slack;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(value = "notifications.slack.enabled")
public class SlackDigestService {

    private static final Logger LOG = LoggerFactory.getLogger(SlackDigestService.class);

    private final SlackSender slackSender;
    private final EventApiClient eventApiClient;

    @Value("${notifications.feed}")
    private String eventApiFeed;

    public SlackDigestService(SlackSender slackSender,
                              EventApiClient eventApiClient) {
        this.slackSender = slackSender;
        this.eventApiClient = eventApiClient;
    }

    @Scheduled(cron = "${notifications.slack.digestCron:0 0 18 * * *}")
    public void sendDigest() {
        OffsetDateTime startOfDay = OffsetDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        Optional<EventApiClient.EventApiSearchEventResponse> critical = eventApiClient.getEvents(eventApiFeed,
                startOfDay, null, 1, EventApiClient.SortOrder.ASC);
        if (critical.isPresent()) {
            return;
        }

        Optional<EventApiClient.EventApiSearchEventResponse> minorResponse =
                eventApiClient.getEventsBySeverities(eventApiFeed, startOfDay, List.of("MINOR"), 100,
                        EventApiClient.SortOrder.ASC);
        if (minorResponse.isEmpty()) {
            return;
        }
        List<EventApiEventDto> events = minorResponse.get().getData();
        if (events.isEmpty()) {
            return;
        }

        String list = events.stream()
                .map(EventApiEventDto::getName)
                .collect(Collectors.joining("\n• ", "• ", ""));
        String text = String.format("{\"text\":\"Non-critical disasters today:\n%s\"}", list.replace("\"", "\\\""));
        slackSender.send(text);
        LOG.info("Sent Slack digest about {} non-critical disasters", events.size());
    }
}
