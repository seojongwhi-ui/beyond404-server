package com.swapit.dto;

import jakarta.validation.constraints.NotBlank;

public record PhotoUploadRequest(
        @NotBlank String fileName,
        String exteriorPhotoFileName,
        String labelPhotoFileName,
        String imageUrl,
        String exteriorImageUrl,
        String labelImageUrl,
        String applianceType,
        String brand,
        String modelName,
        String estimatedAge,
        String exteriorCondition,
        Boolean agreedToCreditPolicy
) {
}
