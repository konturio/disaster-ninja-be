package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.bivariatematrix.BivariateMatrixDto;
import io.kontur.disasterninja.dto.bivariatematrix.BivariateMatrixRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BivariateMatrixService {
    private static final Logger LOG = LoggerFactory.getLogger(BivariateMatrixService.class);

    private final InsightsApiGraphqlClient insightsApiGraphqlClient;

        public BivariateMatrixDto getDataForBivariateMatrix(BivariateMatrixRequestDto bivariateMatrixRequestDto) {
        try {
            return insightsApiGraphqlClient.getBivariateMatrix(bivariateMatrixRequestDto.getGeoJSON(),
                    bivariateMatrixRequestDto.getImportantLayers()).get();
        } catch (Exception e) {
            LOG.error("Can't load bivariate matrix data due to exception in graphql call: {}", e.getMessage(), e);
            throw new WebApplicationException("Exception when getting data from insights-api using apollo client",
                    HttpStatus.BAD_GATEWAY);
        }
    }
}
