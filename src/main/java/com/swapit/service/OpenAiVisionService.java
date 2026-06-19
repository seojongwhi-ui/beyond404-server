package com.swapit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiVisionService {
    private static final String CHAT_COMPLETIONS_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4o";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.vision-model:gpt-4o}")
    private String visionModel;

    public Optional<OpenAiVisionResult> identifyAppliance(String imageReference, String applianceType) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("OPENAI_API_KEY is not configured. Appliance identification will remain unknown.");
            return Optional.empty();
        }

        try {
            Optional<String> resolvedImageUrl = resolveVisionImageUrl(imageReference);
            if (resolvedImageUrl.isEmpty()) {
                log.info("No readable image URL or local image file was provided. Appliance identification will remain unknown.");
                return Optional.empty();
            }

            HttpRequest request = HttpRequest.newBuilder(URI.create(CHAT_COMPLETIONS_ENDPOINT))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(resolvedImageUrl.get(), applianceType)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("OpenAI Vision API failed with status {} and body {}", response.statusCode(), response.body());
                return Optional.empty();
            }

            String content = objectMapper.readTree(response.body())
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("");

            return Optional.of(parseResult(content));
        } catch (IOException | InterruptedException | IllegalArgumentException error) {
            if (error instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Failed to identify appliance with OpenAI Vision. Appliance identification will remain unknown.", error);
            return Optional.empty();
        }
    }

    private String buildRequestBody(String imageUrl, String applianceType) throws IOException {
        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text", """
                Find the appliance brand and exact model name from this image.
                Appliance type hint: %s.
                Reply only with valid JSON in this shape:
                {"brand":"brand","modelName":"model"}
                If you cannot determine either field, use "unknown" for that field.
                """.formatted(valueOrDefault(applianceType, "unknown")));

        Map<String, Object> imageUrlContent = new LinkedHashMap<>();
        imageUrlContent.put("type", "image_url");
        imageUrlContent.put("image_url", Map.of("url", imageUrl));

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", List.of(textContent, imageUrlContent));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", valueOrDefault(visionModel, DEFAULT_MODEL));
        body.put("messages", List.of(message));
        body.put("max_tokens", 200);
        body.put("temperature", 0);

        return objectMapper.writeValueAsString(body);
    }

    OpenAiVisionResult parseResult(String content) throws IOException {
        String json = extractJsonObject(content);
        JsonNode root = objectMapper.readTree(json);
        String brand = normalizeResultField(root.path("brand").asText(""));
        String modelName = normalizeResultField(root.path("modelName").asText(""));
        return new OpenAiVisionResult(
                valueOrDefault(brand, "unknown"),
                valueOrDefault(modelName, "unknown")
        );
    }

    Optional<String> resolveVisionImageUrl(String imageReference) throws IOException {
        if (imageReference == null || imageReference.isBlank()) {
            return Optional.empty();
        }

        String trimmed = imageReference.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("data:image/")) {
            return Optional.of(trimmed);
        }

        Path imagePath = trimmed.startsWith("file:")
                ? Path.of(URI.create(trimmed))
                : Path.of(trimmed);

        if (!Files.isRegularFile(imagePath)) {
            return Optional.empty();
        }

        String mimeType = Files.probeContentType(imagePath);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            mimeType = "image/jpeg";
        }

        String encoded = Base64.getEncoder().encodeToString(Files.readAllBytes(imagePath));
        return Optional.of("data:" + mimeType + ";base64," + encoded);
    }

    private String extractJsonObject(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("OpenAI response content is empty.");
        }

        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("OpenAI response did not contain a JSON object.");
        }

        return content.substring(start, end + 1);
    }

    private String normalizeResultField(String value) {
        return value == null ? "" : value.trim();
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record OpenAiVisionResult(String brand, String modelName) {
        public static OpenAiVisionResult unknown() {
            return new OpenAiVisionResult("unknown", "unknown");
        }
    }
}
