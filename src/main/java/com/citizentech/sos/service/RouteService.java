package com.citizentech.sos.service;

import com.citizentech.sos.dto.RouteDTO.RiskLevel;
import com.citizentech.sos.dto.RouteDTO.RouteResponse;
import com.citizentech.sos.repository.DangerousLocationRepository;
import com.citizentech.sos.repository.SensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pela análise de riscos em rotas da plataforma CitizenTech.
 * Avalia o trajeto entre dois pontos geográficos por meio de amostras de coordenadas.
 * Identifica locais perigosos e sensores inativos próximos ao percurso.
 * Calcula a distância entre coordenadas utilizando a fórmula de Haversine.
 * Atribui pesos diferentes aos riscos identificados para calcular a pontuação total.
 * Classifica a rota em níveis de risco baixo, médio ou alto.
 * Retorna um resumo da avaliação para auxiliar na escolha de trajetos mais seguros.
 */

@Service
public class RouteService {
    private static final int SAMPLES = 20;
    private static final double RADIUS_METERS = 150;
    private static final int DANGEROUS_LOCATION_WEIGHT = 1;
    private static final int FAILED_SENSOR_WEIGHT = 2;
    private static final int MEDIUM_RISK_LIMIT = 10;

    private final DangerousLocationRepository locations;
    private final SensorRepository sensors;

    public RouteService(DangerousLocationRepository locations, SensorRepository sensors) {
        this.locations = locations;
        this.sensors = sensors;
    }

    @Transactional(readOnly = true)
    public RouteResponse rate(double originLat, double originLng, double destLat, double destLng) {
        var dangerousLocations = locations.findByActiveTrue();
        var failedSensors = sensors.findAll().stream().filter(s -> !s.isActive()).toList();

        int riskPoints = 0, totalRisk = 0;
        for (int i = 0; i <= SAMPLES; i++) {
            double t = (double) i / SAMPLES;
            double lat = originLat + (destLat - originLat) * t;
            double lng = originLng + (destLng - originLng) * t;

            for (var location : dangerousLocations) {
                if (distanceMeters(lat, lng, location.getLatitude(), location.getLongitude()) <= RADIUS_METERS) {
                    riskPoints++;
                    totalRisk += DANGEROUS_LOCATION_WEIGHT;
                }
            }
            for (var sensor : failedSensors) {
                if (distanceMeters(lat, lng, sensor.getLatitude(), sensor.getLongitude()) <= RADIUS_METERS) {
                    riskPoints++;
                    totalRisk += FAILED_SENSOR_WEIGHT;
                }
            }
        }

        RiskLevel level = totalRisk == 0 ? RiskLevel.LOW
                : totalRisk <= MEDIUM_RISK_LIMIT ? RiskLevel.MEDIUM : RiskLevel.HIGH;
        return new RouteResponse(riskPoints, totalRisk, level,
                "Estimated risk along the direct path, considering reported locations and failed sensors.");
    }

    static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * earthRadius * Math.asin(Math.sqrt(a));
    }
}
