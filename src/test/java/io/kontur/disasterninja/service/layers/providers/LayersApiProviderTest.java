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

import static io.kontur.disasterninja.util.TestUtil.*;
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

        verify(client, times(1)).findLayers(geometry, false,
                CollectionOwner.ANY, appId);
    }

    @Test
    public void getLayersUserIsNotAuthenticatedWithoutGeometryTest() {
        givenUserIsNotAuthenticated();

        UUID appId = UUID.randomUUID();
        provider.obtainLayers(LayerSearchParams.builder().boundary(null).appId(appId).build());

        verify(client, times(1)).findLayers(null, true,
                CollectionOwner.ANY, appId);
    }

    @Test
    public void getLayersUserIsAuthenticatedTest() throws JsonProcessingException {
        givenUserIsLoggedIn();

        Geometry geometry = objectMapper.readValue(json, Geometry.class);
        UUID appId = UUID.randomUUID();
        provider.obtainLayers(LayerSearchParams.builder().boundary(geometry).appId(appId).build());

        verify(client, times(1)).findLayers(null, false,
                CollectionOwner.ME, appId);
        verify(client, times(1)).findLayers(geometry, false,
                CollectionOwner.NOT_ME, appId);
    }

    @Test
    public void getLayersUserIsAuthenticatedWithoutGeometryTest() {
        givenUserIsLoggedIn();

        UUID appId = UUID.randomUUID();
        provider.obtainLayers(LayerSearchParams.builder().boundary(null).appId(appId).build());

        verify(client, times(1)).findLayers(null, false,
                CollectionOwner.ME, appId);
        verify(client, times(1)).findLayers(null, true,
                CollectionOwner.NOT_ME, appId);
    }

    @Test
    public void obtainGlobalLayersTest() {
        givenUserIsNotAuthenticated();

        provider.obtainGlobalLayers(emptyParams());

        verify(client, times(1)).findLayers(null, true,
                CollectionOwner.ANY, null);
    }

    @Test
    public void obtainUserLayersTest() {
        givenUserIsLoggedIn();

        UUID appId = UUID.randomUUID();
        provider.obtainUserLayers(LayerSearchParams.builder().boundary(null).appId(appId).build());

        verify(client, times(1)).findLayers(null, false,
                CollectionOwner.ME, appId);
    }

    @Test
    public void obtainSelectedAreaLayersUserIsNotAuthenticatedTest() throws JsonProcessingException {
        givenUserIsNotAuthenticated();

        Geometry geometry = objectMapper.readValue(json, Geometry.class);
        UUID appId = UUID.randomUUID();
        provider.obtainSelectedAreaLayers(LayerSearchParams.builder().boundary(geometry).appId(appId).build());

        verify(client, times(1)).findLayers(geometry, false,
                CollectionOwner.ANY, appId);
    }

    @Test
    public void obtainSelectedAreaLayersUserIsAuthenticatedTest() throws JsonProcessingException {
        givenUserIsLoggedIn();

        Geometry geometry = objectMapper.readValue(json, Geometry.class);
        UUID appId = UUID.randomUUID();
        provider.obtainSelectedAreaLayers(LayerSearchParams.builder().boundary(geometry).appId(appId).build());

        verify(client, times(1)).findLayers(geometry, false,
                CollectionOwner.NOT_ME, appId);
    }
}
