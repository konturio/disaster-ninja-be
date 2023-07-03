package io.kontur.disasterninja.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Objects;

@RestController
@Hidden
public class ChatGptPluginController {

    @GetMapping("/.well-known/ai-plugin.json")
    public ResponseEntity<InputStreamResource> getPluginManifest() {
        InputStream pluginManifest = getClass().getClassLoader().getResourceAsStream("ai-plugin.json");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new InputStreamResource(Objects.requireNonNull(pluginManifest)));
    }

    @GetMapping("/openapi.json")
    public ResponseEntity<InputStreamResource> getOpenApiSpec() {
        InputStream openApiSpec = getClass().getClassLoader().getResourceAsStream("openapi.json");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new InputStreamResource(Objects.requireNonNull(openApiSpec)));
    }
}
