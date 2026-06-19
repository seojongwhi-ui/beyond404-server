package com.swapit.dto;

import java.math.BigDecimal;

public record ApplianceSpecLookupResponse(
        String brand,
        String applianceType,
        String modelName,
        String sizeGrade,
        String sizeMetric,
        BigDecimal weightKg
) {
}
