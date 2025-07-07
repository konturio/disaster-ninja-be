package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.notifications.slack.SlackMessageFormatter;
import io.kontur.disasterninja.notifications.slack.SlackNotificationService;
import io.kontur.disasterninja.notifications.slack.SlackSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationServicesConfig {

    @Bean
    @ConditionalOnProperty("notifications.slack.enabled")
    public SlackNotificationService slackNotificationService(
            SlackMessageFormatter formatter,
            SlackSender sender,
            @Value("${notifications.feed}") String feed,
            @Value("${notifications.slackWebHook}") String hook) {
        return new SlackNotificationService(formatter, sender, feed, hook);
    }

    @Bean
    @ConditionalOnProperty("notifications.slack2.enabled")
    public SlackNotificationService slackNotificationService2(
            SlackMessageFormatter formatter,
            SlackSender sender,
            @Value("${notifications.feed2}") String feed,
            @Value("${notifications.slackWebHook2}") String hook) {
        // customize formatter or request parameters for this receiver if needed
        return new SlackNotificationService(formatter, sender, feed, hook);
    }
}
