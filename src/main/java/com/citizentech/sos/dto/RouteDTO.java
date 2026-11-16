package com.citizentech.sos.dto;

public final class RouteDTO {
    private RouteDTO() {}

    public enum RiskLevel { LOW, MEDIUM, HIGH }

    public record RouteResponse(int riskPoints, int totalRisk, RiskLevel level, String note) {}
}
