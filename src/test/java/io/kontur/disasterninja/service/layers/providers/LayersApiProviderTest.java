package io.kontur.disasterninja.service.layers.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.client.TestDependingOnUserAuth;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.dto.layerapi.CollectionOwner;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.wololo.geojson.Geometry;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class LayersApiProviderTest extends TestDependingOnUserAuth {
    @Mock
    LayersApiClient client = mock(LayersApiClient.class);
    LayersApiProvider provider = new LayersApiProvider(client);
    ObjectMapper objectMapper = new ObjectMapper();

    String json = "{\"type\":\"Polygon\",\"coordinates\":[[[1.83975,6.2578],[1.83975,7.11427],[2.5494,7.11427]," +
            "[2.5494,6.48905],[2.49781,6.25806],[1.83975,6.2578]]]}";

    @Test
    public void getLayersUserIsNotAuthenticatedTest() throws JsonProcessingException {
        givenUserIsNotAuthenticated();

        Geometry geometry = objectMapper.readValue(json, Geometry.class);
        UUID appId = UUID.randomUUID();
        provider.obtainLayers(LayerSearchParams.builder().boundary(geometry).appId(appId).build());

        verify(client, times(1)).findLayers(geometry, CollectionOwner.ANY, appId);
    }

    @Test
    public void getLayersUserIsAuthenticatedTest() throws JsonProcessingException {
        givenUserIsLoggedIn();

        Geometry geometry = objectMapper.readValue(json, Geometry.class);
        UUID appId = UUID.randomUUID();
        provider.obtainLayers(LayerSearchParams.builder().boundary(geometry).appId(appId).build());

        verify(client, times(1)).findLayers(null, CollectionOwner.ME, appId);
        verify(client, times(1)).findLayers(geometry, CollectionOwner.NOT_ME, appId);
    }
}
