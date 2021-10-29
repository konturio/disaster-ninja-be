package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.dto.AnalyticsField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "analyticstab")
@Getter
@Setter
public class AnalyticsTabProperties {

    private List<AnalyticsField> fields;

}
