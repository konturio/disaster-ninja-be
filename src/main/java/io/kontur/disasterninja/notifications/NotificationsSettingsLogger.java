package io.kontur.disasterninja.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Logs notification related configuration values on startup to help diagnose
 * why certain notification services might not be initialized.
 */
@Component
public class NotificationsSettingsLogger {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationsSettingsLogger.class);

    @Value("${notifications.enabled:false}")
    private boolean notificationsEnabled;

    @Value("${notifications.slack.enabled:false}")
    private boolean slackEnabled;

    @Value("${notifications.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notifications.feed:}")
    private String eventApiFeed;

    @Value("${notifications.slackWebHook:}")
    private String slackWebHookUrl;

    @Value("${notifications.sender:}")
    private String sender;

    @Value("${notifications.recipients:}")
    private String[] recipients;

    @PostConstruct
    public void logSettings() {
        LOG.info("Notification settings - enabled: {}, slack.enabled: {}, email.enabled: {}, feed: {}",
                notificationsEnabled, slackEnabled, emailEnabled, eventApiFeed);
        LOG.info("Slack webhook {}configured", slackWebHookUrl == null || slackWebHookUrl.isEmpty() ? "NOT " : "");
        LOG.info("Email sender: '{}' recipients: {}", sender, Arrays.toString(recipients));
    }
}
