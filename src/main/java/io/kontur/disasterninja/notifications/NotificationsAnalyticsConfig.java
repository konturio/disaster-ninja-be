package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.dto.Function;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "notificationsanalyticsconfig")
@Getter
@Setter
public class NotificationsAnalyticsConfig {

    private List<Function> functions;

}
