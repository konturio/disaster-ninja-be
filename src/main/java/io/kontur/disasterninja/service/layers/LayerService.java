package io.kontur.disasterninja.service.layers;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSearchParams;
import io.kontur.disasterninja.dto.layer.LayerCreateDto;
import io.kontur.disasterninja.dto.layer.LayerUpdateDto;
import io.kontur.disasterninja.service.layers.providers.LayerProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.wololo.geojson.FeatureCollection;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class LayerService {

    private static final Logger LOG = LoggerFactory.getLogger(LayerService.class);
    final LocalLayerConfigService layerConfigService;
    final List<LayerProvider> providers;
    private final LayersApiClient layersApiClient;

    public Layer create(LayerCreateDto dto) {
        return layersApiClient.createLayer(dto);
    }

    public Layer update(String id, LayerUpdateDto dto) {
        return layersApiClient.updateLayer(id, dto);
    }

    public void delete(String id) {
        layersApiClient.deleteLayer(id);
    }

    public List<Layer> getGlobalLayers(LayerSearchParams layerSearchParams) {
        Map<String, Layer> layers = loadLayersFromProviders(LayerProvider::obtainGlobalLayers, layerSearchParams);
        applyLayerConfig(layers);
        //add global overlays
        layerConfigService.getGlobalOverlays().forEach((id, config) -> {
            if (!layers.containsKey(id)) { //can be already loaded by a provider
                layers.put(id, config);
            }
        });
        return layersMapToList(layers);
    }

    public List<Layer> getUserLayers(LayerSearchParams layerSearchParams) {
        Map<String, Layer> layers = loadLayersFromProviders(LayerProvider::obtainUserLayers, layerSearchParams);
        applyLayerConfig(layers);
        return layersMapToList(layers);
    }

    public List<Layer> getSelectedAreaLayers(LayerSearchParams layerSearchParams) {
        Map<String, Layer> layers = loadLayersFromProviders(LayerProvider::obtainSelectedAreaLayers, layerSearchParams);
        applyLayerConfig(layers);
        return layersMapToList(layers);
    }

    private Map<String, Layer> loadLayersFromProviders(BiFunction<LayerProvider, LayerSearchParams,
            CompletableFuture<List<Layer>>> obtainLayers, LayerSearchParams layerSearchParams) {
        return providers.stream()
                .map(provider -> {
                    try {
                        return obtainLayers.apply(provider, layerSearchParams)
                                .handle((l, ex) -> {
                                    if (ex != null) {
                                        if (ex.getCause() instanceof HttpClientErrorException) {
                                            LOG.info("Client error occurred while obtaining layers from {}: {}",
                                                    provider.getClass().getSimpleName(), ex.getMessage(), ex);
                                        } else {
                                            LOG.error("Caught exception while obtaining layers from {}: {}",
                                                    provider.getClass().getSimpleName(), ex.getMessage(), ex);
                                        }
                                        return Collections.<Layer>emptyList();
                                    }
                                    return l;
                                });
                    } catch (Exception e) {
                        if (e.getCause() instanceof HttpClientErrorException) {
                            LOG.info("Client error occurred while obtaining layers from {}: {}",
                                    provider.getClass().getSimpleName(), e.getMessage(), e);
                        } else {
                            LOG.error("Caught exception while obtaining layers from {}: {}",
                                    provider.getClass().getSimpleName(), e.getMessage(), e);
                        }
                        return CompletableFuture.completedFuture(Collections.<Layer>emptyList());
                    }
                })
                .filter(Objects::nonNull)
                .toList()
                .stream()
                .filter(Objects::nonNull)
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Layer::getId, layer -> layer, (layer, layer2) -> layer2));
    }

    private void applyLayerConfig(Map<String, Layer> layers) {
        layers.values().forEach(layerConfigService::applyConfig);
    }

    private List<Layer> layersMapToList(Map<String, Layer> layers) {
        return layers.values().stream()
                .sorted(Comparator.comparing(layer -> Optional.ofNullable(layer.getOrderIndex())
                        .orElse(Integer.MAX_VALUE)))
                .toList();
    }

    public List<Layer> get(List<String> layersToRetrieveWithGeometryFilter,
                           List<String> layersToRetrieveWithoutGeometryFilter, LayerSearchParams searchParams) {
        List<Layer> layersFoundWithGeometryFilter = get(layersToRetrieveWithGeometryFilter, searchParams);
        List<Layer> layersFoundWithoutGeometryFilter = get(layersToRetrieveWithoutGeometryFilter,
                searchParams.getCopyWithoutBoundary());

        return mergeLayerListsByLayerId(layersFoundWithGeometryFilter, layersFoundWithoutGeometryFilter);
    }

    protected List<Layer> get(List<String> layerIds, LayerSearchParams searchParams) {
        List<Layer> result = new ArrayList<>();

        if (isEmpty(layerIds)) {
            return List.of();
        }

        for (String layerId : layerIds) {
            Layer layer = getFromProvidersOrGlobalOverlays(layerId, searchParams);
            if (layer != null) {
                result.add(layer);
            }
        }

        if (result.isEmpty()) {
            throw new WebApplicationException("Layer not found / no layer data found by id and boundary!",
                    HttpStatus.NOT_FOUND);
        }
        return result;
    }

    private Layer getFromProvidersOrGlobalOverlays(String layerId, LayerSearchParams searchParams) {
        Layer layer = getFromProviders(layerId, searchParams);
        if (layer != null) {
            return layer;
        }
        //if not found - use global overlay from config
        layer = layerConfigService.getGlobalOverlays().get(layerId);
        if (layer != null && layer.getSource() != null) {
            LOG.info("No loaded layer found by id, using a global overlay: {}", layerId);
        }
        return layer;
    }

    private Layer getFromProviders(String layerId, LayerSearchParams searchParams) {
        for (LayerProvider provider : providers) {
            //find first by layer id and use it
            if (provider.isApplicable(layerId)) {
                Layer layer = provider.obtainLayer(layerId, searchParams);
                if (layer != null) {
                    LOG.info("Found layer by id {} by provider {}", layerId, provider.getClass().getSimpleName());
                    layerConfigService.applyConfig(layer);
                }
                return layer;
            }
        }
        return null;
    }

    public FeatureCollection updateFeatures(String layerId, FeatureCollection body) {
        return layersApiClient.updateLayerFeatures(layerId, body);
    }

    private List<Layer> mergeLayerListsByLayerId(List<Layer> mainList, List<Layer> otherList) {
        List<Layer> mergedList = new ArrayList<>(mainList);
        otherList.forEach(layer -> {
            String layerId = layer.getId();
            if (mergedList.stream().map(Layer::getId).noneMatch(it -> it.equals(layerId))) {
                mergedList.add(layer);
            }
        });
        return mergedList;
    }
}
