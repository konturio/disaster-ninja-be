package io.kontur.disasterninja.service;

import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AxisService {
    private static final Logger LOG = LoggerFactory.getLogger(AxisService.class);

    private final InsightsApiGraphqlClient insightsApiGraphqlClient;

        public List<BivariateLegendAxisDescription> getDataForAxis(UUID numerator, UUID denominator) {
        try {
            return insightsApiGraphqlClient.getAxisList(numerator, denominator).get();
        } catch (Exception e) {
            LOG.error("Can't load axis due to exception in graphql call: {}", e.getMessage(), e);
            throw new WebApplicationException("Exception when getting data from insights-api using apollo client",
                    HttpStatus.BAD_GATEWAY);
        }
    }
}
