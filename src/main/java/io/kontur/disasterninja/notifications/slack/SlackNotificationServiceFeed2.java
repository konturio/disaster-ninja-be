package io.kontur.disasterninja.notifications.slack;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;

import java.util.Map;

/**
 * Slack notification service for feed2 that ignores applicability checks and
 * omits event links in the message.
 */
public class SlackNotificationServiceFeed2 extends SlackNotificationService {

    public SlackNotificationServiceFeed2(SlackMessageFormatter slackMessageFormatter,
                                         SlackSender slackSender,
                                         String eventApiFeed,
                                         String slackWebHookUrl) {
        super(slackMessageFormatter, slackSender, eventApiFeed, slackWebHookUrl);
    }

    @Override
    protected String formatMessage(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics) {
        return slackMessageFormatter.format(event, urbanPopulationProperties, analytics, false);
    }

    @Override
    public boolean isApplicable(EventApiEventDto event) {
        return true;
    }

    // use inherited getEventApiFeed()
}
