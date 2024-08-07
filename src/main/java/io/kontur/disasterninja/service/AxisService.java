package io.kontur.disasterninja.service;

import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import io.kontur.disasterninja.domain.Transformation;
import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AxisService {
    private static final Logger LOG = LoggerFactory.getLogger(AxisService.class);

    private final InsightsApiGraphqlClient insightsApiGraphqlClient;

    public List<Transformation> getTransformations(String numerator, String denominator) {
        try {
            return insightsApiGraphqlClient.getTransformationList(numerator, denominator).get();
        } catch (Exception e) {
            LOG.error("Can't load transformations due to exception in graphql call: {}", e.getMessage(), e);
            throw new WebApplicationException("Exception when getting data from insights-api using apollo client",
                    HttpStatus.BAD_GATEWAY);
        }
    }

    public List<BivariateLegendAxisDescription> getDataForAxis() {
        try {
            return insightsApiGraphqlClient.getAxisList().get();
        } catch (Exception e) {
            LOG.error("Can't load axis due to exception in graphql call: {}", e.getMessage(), e);
            throw new WebApplicationException("Exception when getting data from insights-api using apollo client",
                    HttpStatus.BAD_GATEWAY);
        }
    }
}
