package io.kontur.disasterninja.dto.layer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LayerSummaryDtoTest {

    @Test
    public void testMapBoxStyle_userSettingsPositive() {
        Layer layer = createLayer();
        layer.setMapboxStyles(JsonUtil.readJson("{\"url\":\"https://d.n/style_{lang}.json\",\"lang\":[\"kz\",\"ar\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "en-BY,en;q=0.9,ru-BY;q=0.8,ru;q=0.7,en-GB;q=0.6,en-US;q=0.5");
        request.addHeader("user-language", "ar");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertEquals("https://d.n/style_ar.json", dto.getMapboxStyle());
    }

    @Test
    public void testMapBoxStyle_noUserSettings() {
        Layer layer = createLayer();
        layer.setMapboxStyles(JsonUtil.readJson("{\"url\":\"https://d.n/style_{lang}.json\",\"lang\":[\"en\",\"ar\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "ar,en;q=0.9,ru-BY;q=0.8,ru;q=0.7,en-GB;q=0.6,en-US;q=0.5");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertEquals("https://d.n/style_ar.json", dto.getMapboxStyle());
    }

    @Test
    public void testMapBoxStyle_AcceptLanguageFirstWin() {
        Layer layer = createLayer();
        layer.setMapboxStyles(JsonUtil.readJson("{\"url\":\"https://d.n/style_{lang}.json\",\"lang\":[\"en\",\"ar\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "ar,en;q=0.9,ru-BY;q=0.8,ru;q=0.7,en-GB;q=0.6,en-US;q=0.5");
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertEquals("https://d.n/style_ar.json", dto.getMapboxStyle());
    }

    @Test
    public void testMapBoxStyle_AcceptLanguageSecondWin() {
        Layer layer = createLayer();
        layer.setMapboxStyles(JsonUtil.readJson("{\"url\":\"https://d.n/style_{lang}.json\",\"lang\":[\"en\",\"th\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "ar,en;q=0.9,ru-BY;q=0.8,ru;q=0.7,en-GB;q=0.6,en-US;q=0.5");
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertEquals("https://d.n/style_en.json", dto.getMapboxStyle());
    }

    @Test
    public void testMapBoxStyle_AcceptLanguageLastWin() {
        Layer layer = createLayer();
        layer.setMapboxStyles(JsonUtil.readJson("{\"url\":\"https://d.n/style_{lang}.json\",\"lang\":[\"ru\",\"th\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "ar,en;q=0.9,ru-BY;q=0.8,ru;q=0.7,en-GB;q=0.6,en-US;q=0.5");
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertEquals("https://d.n/style_ru.json", dto.getMapboxStyle());
    }

    @Test
    public void testMapBoxStyle_NoMatchUseEn() {
        Layer layer = createLayer();
        layer.setMapboxStyles(JsonUtil.readJson("{\"url\":\"https://d.n/style_{lang}.json\",\"lang\":[\"th\",\"en\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "ar;q=0.9,ru-BY;q=0.8,ru;q=0.7");
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertEquals("https://d.n/style_en.json", dto.getMapboxStyle());
    }

    @Test
    public void testMapBoxStyle_NoMatchAndNoSupportedEn() {
        Layer layer = createLayer();
        layer.setMapboxStyles(JsonUtil.readJson("{\"url\":\"https://d.n/style_{lang}.json\",\"lang\":[\"th\",\"kz\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "ar,en;q=0.9,ru-BY;q=0.8,ru;q=0.7,en-GB;q=0.6,en-US;q=0.5");
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertEquals("https://d.n/style_{lang}.json", dto.getMapboxStyle());
    }

    @Test
    public void testMapBoxStyle_NoMapboxStyle() {
        Layer layer = createLayer();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "ar,en;q=0.9,ru-BY;q=0.8,ru;q=0.7,en-GB;q=0.6,en-US;q=0.5");
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertNull(dto.getMapboxStyle());
    }

    @Test
    public void testMapBoxStyle_EmptyMapboxStyle() {
        Layer layer = createLayer();
        layer.setMapboxStyles(JsonUtil.readJson("{}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "ar,en;q=0.9,ru-BY;q=0.8,ru;q=0.7,en-GB;q=0.6,en-US;q=0.5");
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertNull(dto.getMapboxStyle());
    }

    @Test
    public void testMapBoxStyle_NoUrlInMapboxStyle() {
        Layer layer = createLayer();
        layer.setMapboxStyles(JsonUtil.readJson("{\"lang\":[\"th\",\"kz\"]}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "ar,en;q=0.9,ru-BY;q=0.8,ru;q=0.7,en-GB;q=0.6,en-US;q=0.5");
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertNull(dto.getMapboxStyle());
    }

    @Test
    public void testMapBoxStyle_NoLangInMapboxStyle() {
        Layer layer = createLayer();
        layer.setMapboxStyles(JsonUtil.readJson("{\"url\":\"https://d.n/style_{lang}.json\"}", ObjectNode.class));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("accept-language", "ar,en;q=0.9,ru-BY;q=0.8,ru;q=0.7,en-GB;q=0.6,en-US;q=0.5");
        request.addHeader("user-language", "ja");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        LayerSummaryDto dto = LayerSummaryDto.fromLayer(layer);

        assertEquals("https://d.n/style_{lang}.json", dto.getMapboxStyle());
    }

    private Layer createLayer() {
        return Layer.builder()
                .id("id")
                .name("name")
                .build();
    }
}
