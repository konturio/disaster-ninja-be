package io.kontur.disasterninja.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@RestController
@Hidden
public class ChatGptPluginController {

    @GetMapping("/.well-known/ai-plugin.json")
    public ResponseEntity<InputStreamResource> getPluginManifest() {
        try (InputStream pluginManifest = getClass().getClassLoader().getResourceAsStream("ai-plugin.json")) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new InputStreamResource(Objects.requireNonNull(pluginManifest)));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/openapi.json")
    public ResponseEntity<InputStreamResource> getOpenApiSpec() {
        try (InputStream openApiSpec = getClass().getClassLoader().getResourceAsStream("openapi.json")) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new InputStreamResource(Objects.requireNonNull(openApiSpec)));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
