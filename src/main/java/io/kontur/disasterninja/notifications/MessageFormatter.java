package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.dto.Severity;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.dto.eventapi.FeedEpisode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@ConditionalOnProperty(value = "notifications.enabled")
public class MessageFormatter {

    protected FeedEpisode getLatestEpisode(EventApiEventDto event) {
        List<FeedEpisode> eventEpisodes = event.getEpisodes();
        eventEpisodes.sort(Comparator.comparing(FeedEpisode::getUpdatedAt));

        return eventEpisodes.get(eventEpisodes.size() - 1);
    }

    protected String getMessageColorCode(EventApiEventDto event, FeedEpisode latestEpisode, boolean unicode) {
        String name = event.getName();

        if (StringUtils.isNotBlank(name) && name.startsWith("Green")) {
            return (unicode ? "🟢 " : ":large_green_circle: ");
        } else if (StringUtils.isNotBlank(name) && name.startsWith("Orange")) {
            return (unicode ? "🟠 " : ":large_orange_circle: ");
        } else if (StringUtils.isNotBlank(name) && name.startsWith("Red")) {
            return (unicode ? "🔴 " : ":red_circle: ");
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

    protected String getEventStatus(EventApiEventDto event) {
        return event.getVersion() == 1 ? "" : "[Update] ";
    }
}
