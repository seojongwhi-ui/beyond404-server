package com.swapit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "swapit.crew-settlement")
public class CrewSettlementProperties {
    private String currency;
    private int baseFee;
    private int distanceUnitMeters;
    private int feePerDistanceUnit;
    private int minimumDistanceFee;
    private int maximumDistanceFee;
    private double fallbackDistanceMeters;
    private int scheduledCallIncentive;
    private int instantCallIncentive;
    private int completionIncentive;
    private int penaltyPerCount;
    private int minimumTotal;
    private Map<String, Integer> applianceSizeSurcharges = new HashMap<>();
    private Map<String, Integer> applianceTypeSurcharges = new HashMap<>();

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(int baseFee) {
        this.baseFee = baseFee;
    }

    public int getDistanceUnitMeters() {
        return distanceUnitMeters;
    }

    public void setDistanceUnitMeters(int distanceUnitMeters) {
        this.distanceUnitMeters = distanceUnitMeters;
    }

    public int getFeePerDistanceUnit() {
        return feePerDistanceUnit;
    }

    public void setFeePerDistanceUnit(int feePerDistanceUnit) {
        this.feePerDistanceUnit = feePerDistanceUnit;
    }

    public int getMinimumDistanceFee() {
        return minimumDistanceFee;
    }

    public void setMinimumDistanceFee(int minimumDistanceFee) {
        this.minimumDistanceFee = minimumDistanceFee;
    }

    public int getMaximumDistanceFee() {
        return maximumDistanceFee;
    }

    public void setMaximumDistanceFee(int maximumDistanceFee) {
        this.maximumDistanceFee = maximumDistanceFee;
    }

    public double getFallbackDistanceMeters() {
        return fallbackDistanceMeters;
    }

    public void setFallbackDistanceMeters(double fallbackDistanceMeters) {
        this.fallbackDistanceMeters = fallbackDistanceMeters;
    }

    public int getScheduledCallIncentive() {
        return scheduledCallIncentive;
    }

    public void setScheduledCallIncentive(int scheduledCallIncentive) {
        this.scheduledCallIncentive = scheduledCallIncentive;
    }

    public int getInstantCallIncentive() {
        return instantCallIncentive;
    }

    public void setInstantCallIncentive(int instantCallIncentive) {
        this.instantCallIncentive = instantCallIncentive;
    }

    public int getCompletionIncentive() {
        return completionIncentive;
    }

    public void setCompletionIncentive(int completionIncentive) {
        this.completionIncentive = completionIncentive;
    }

    public int getPenaltyPerCount() {
        return penaltyPerCount;
    }

    public void setPenaltyPerCount(int penaltyPerCount) {
        this.penaltyPerCount = penaltyPerCount;
    }

    public int getMinimumTotal() {
        return minimumTotal;
    }

    public void setMinimumTotal(int minimumTotal) {
        this.minimumTotal = minimumTotal;
    }

    public Map<String, Integer> getApplianceSizeSurcharges() {
        return applianceSizeSurcharges;
    }

    public void setApplianceSizeSurcharges(Map<String, Integer> applianceSizeSurcharges) {
        this.applianceSizeSurcharges = applianceSizeSurcharges;
    }

    public Map<String, Integer> getApplianceTypeSurcharges() {
        return applianceTypeSurcharges;
    }

    public void setApplianceTypeSurcharges(Map<String, Integer> applianceTypeSurcharges) {
        this.applianceTypeSurcharges = applianceTypeSurcharges;
    }
}
