package io.kontur.disasterninja.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtil {

    private static final ObjectMapper mapper;

    static {
        mapper = JsonMapper
                .builder()
                .addModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> T readJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readJson(String json, TypeReference<T> valueTypeRef) {
        try {
            return mapper.readValue(json, valueTypeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String writeJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readObjectNode(TreeNode node, Class<T> clazz) {
        try {
            return mapper.treeToValue(node, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectNode writeObjectNode(Object object) {
        return mapper.valueToTree(object);
    }
}
