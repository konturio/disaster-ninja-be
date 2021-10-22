package io.kontur.disasterninja.config;

import io.kontur.disasterninja.config.logging.LogHttpTraceRepository;
import org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "management.trace.http", name = "enabled", matchIfMissing = true)
@AutoConfigureBefore(HttpTraceAutoConfiguration.class)
public class TraceConfig {

    @Bean
    public LogHttpTraceRepository traceRepository() {
        return new LogHttpTraceRepository();
    }
}