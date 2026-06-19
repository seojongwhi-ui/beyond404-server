package com.swapit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiVisionServiceTest {
    private final OpenAiVisionService service = new OpenAiVisionService(new ObjectMapper());

    @TempDir
    Path tempDir;

    @Test
    void parseResultExtractsJsonFromMarkdownFence() throws Exception {
        OpenAiVisionService.OpenAiVisionResult result = service.parseResult("""
                ```json
                {"brand":"LG","modelName":"OLED55C4"}
                ```
                """);

        assertThat(result.brand()).isEqualTo("LG");
        assertThat(result.modelName()).isEqualTo("OLED55C4");
    }

    @Test
    void parseResultUsesUnknownForBlankFields() throws Exception {
        OpenAiVisionService.OpenAiVisionResult result = service.parseResult("""
                {"brand":"","modelName":"   "}
                """);

        assertThat(result.brand()).isEqualTo("unknown");
        assertThat(result.modelName()).isEqualTo("unknown");
    }

    @Test
    void resolveVisionImageUrlKeepsDataUrl() throws Exception {
        String dataUrl = "data:image/jpeg;base64,abcd";

        assertThat(service.resolveVisionImageUrl(dataUrl)).contains(dataUrl);
    }

    @Test
    void resolveVisionImageUrlEncodesLocalFileAsDataUrl() throws Exception {
        Path image = tempDir.resolve("capture.jpg");
        Files.write(image, new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00});

        assertThat(service.resolveVisionImageUrl(image.toString()))
                .hasValueSatisfying(value -> assertThat(value).startsWith("data:image/"));
    }
}
