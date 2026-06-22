package com.swapit.service;

import com.swapit.config.CrewSettlementProperties;
import com.swapit.dto.SwapRequestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class CrewSettlementCalculator {
    private static final String STATUS_ESTIMATED = "ESTIMATED";

    private final CrewSettlementProperties properties;

    public SwapRequestResponse.Settlement estimate(SwapRequestResponse response) {
        return calculate(response, STATUS_ESTIMATED);
    }

    public SwapRequestResponse.Settlement finalizeSettlement(SwapRequestResponse response) {
        return calculate(response, "READY");
    }

    private SwapRequestResponse.Settlement calculate(SwapRequestResponse response, String status) {
        if (response.pickupRequest() == null) {
            return null;
        }

        int baseFee = properties.getBaseFee();
        int distanceFee = calculateDistanceFee(resolveDistanceMeters(response));
        int incentive = calculateIncentive(response);
        int penalty = calculatePenalty(response);
        int totalAmount = Math.max(properties.getMinimumTotal(), baseFee + distanceFee + incentive - penalty);

        return new SwapRequestResponse.Settlement(
                baseFee,
                distanceFee,
                incentive,
                penalty,
                totalAmount,
                properties.getCurrency(),
                status
        );
    }

    private int calculateDistanceFee(Double distanceMeters) {
        double resolvedDistance = distanceMeters == null ? properties.getFallbackDistanceMeters() : distanceMeters;
        int unitMeters = Math.max(1, properties.getDistanceUnitMeters());
        int units = Math.max(1, (int) Math.ceil(resolvedDistance / unitMeters));
        int fee = Math.max(properties.getMinimumDistanceFee(), units * properties.getFeePerDistanceUnit());

        if (properties.getMaximumDistanceFee() > 0) {
            return Math.min(properties.getMaximumDistanceFee(), fee);
        }

        return fee;
    }

    private int calculateIncentive(SwapRequestResponse response) {
        int incentive = properties.getCompletionIncentive();

        String pickupType = response.pickupRequest().pickupType();
        if ("INSTANT_CALL".equals(pickupType)) {
            incentive += properties.getInstantCallIncentive();
        } else if ("BOOKING".equals(pickupType)) {
            incentive += properties.getScheduledCallIncentive();
        }

        if (response.appliance() != null) {
            incentive += properties.getApplianceSizeSurcharges().getOrDefault(normalizeSizeGrade(response.appliance().sizeGrade()), 0);
            incentive += properties.getApplianceTypeSurcharges().getOrDefault(response.appliance().applianceType(), 0);
        }

        return incentive;
    }

    private String normalizeSizeGrade(String sizeGrade) {
        if (sizeGrade == null) {
            return "";
        }

        return switch (sizeGrade) {
            case "소형" -> "small";
            case "중형" -> "medium";
            case "대형" -> "large";
            default -> sizeGrade;
        };
    }

    private int calculatePenalty(SwapRequestResponse response) {
        if (response.dispatchInfo() == null) {
            return 0;
        }

        return Math.max(0, response.dispatchInfo().penaltyCount()) * properties.getPenaltyPerCount();
    }

    private Double resolveDistanceMeters(SwapRequestResponse response) {
        if (response.tracking() != null) {
            if (response.tracking().route() != null && response.tracking().route().distanceMeters() != null) {
                return response.tracking().route().distanceMeters();
            }

            if (response.tracking().metrics() != null && response.tracking().metrics().crewToPickupMeters() != null) {
                return response.tracking().metrics().crewToPickupMeters();
            }
        }

        if (response.pickupRequest().nearbyCrews() == null || response.pickupRequest().nearbyCrews().isEmpty()) {
            return null;
        }

        return response.pickupRequest().nearbyCrews().stream()
                .filter(SwapRequestResponse.NearbyCrew::assigned)
                .findFirst()
                .or(() -> response.pickupRequest().nearbyCrews().stream()
                        .min(Comparator.comparingDouble(SwapRequestResponse.NearbyCrew::distanceMeters)))
                .map(SwapRequestResponse.NearbyCrew::distanceMeters)
                .orElse(null);
    }
}
