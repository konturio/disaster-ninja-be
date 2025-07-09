package io.kontur.disasterninja.notifications.slack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SlackSender {

    private final static Logger LOG = LoggerFactory.getLogger(SlackSender.class);

    @Value("${notifications.slackWebHook:}")
    private String slackWebHookUrl;

    public void send(String message) {
        send(message, slackWebHookUrl);
    }

    public void send(String message, String webHookUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            new RestTemplate().exchange(webHookUrl, HttpMethod.POST, new HttpEntity<>(message, headers), String.class);
        } catch (Exception e) {
            LOG.error("Failed to send slack notification. {}", e.getMessage(), e);
        }
    }
}
