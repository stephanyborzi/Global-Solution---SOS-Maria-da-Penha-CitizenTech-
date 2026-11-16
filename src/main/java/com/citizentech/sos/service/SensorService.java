
package com.citizentech.sos.service;

import com.citizentech.sos.dto.SensorReadingDTO.*;
import com.citizentech.sos.exception.ResourceNotFoundException;
import com.citizentech.sos.model.Sensor;
import com.citizentech.sos.model.SensorReading;
import com.citizentech.sos.model.SensorType;
import com.citizentech.sos.repository.SensorReadingRepository;
import com.citizentech.sos.repository.SensorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pelo gerenciamento dos sensores IoT da plataforma CitizenTech.
 * Realiza o cadastro de sensores e a consulta de seus registros.
 * Gerencia o recebimento e armazenamento das leituras no banco de dados.
 * Valida os valores recebidos de acordo com o tipo de sensor.
 * Identifica possíveis situações de alerta com base nas leituras realizadas.
 * Atualiza o estado dos sensores e disponibiliza o histórico de medições.
 * Utiliza os repositórios para garantir a persistência e consulta dos dados.
 */

@Service
public class SensorService {

    private static final double MIN_LIGHT_PERCENT = 20.0;

    private final SensorRepository sensorRepo;
    private final SensorReadingRepository readingRepo;

    public SensorService(
            SensorRepository sensorRepo,
            SensorReadingRepository readingRepo) {
        this.sensorRepo = sensorRepo;
        this.readingRepo = readingRepo;
    }

    public SensorResponse create(SensorRequest request) {
        Sensor sensor = new Sensor(
                request.type(),
                request.latitude(),
                request.longitude());
        return SensorResponse.from(sensorRepo.save(sensor));
    }

    @Transactional(readOnly = true)
    public List<SensorResponse> list() {
        return sensorRepo.findAll()
                .stream()
                .map(SensorResponse::from)
                .toList();}

    @Transactional
    public ReadingResponse registerReading(
            Long sensorId,
            ReadingRequest request) {
        Sensor sensor = get(sensorId);
        validateReading(sensor.getType(), request.value());
        boolean alert = hasProblem(
                sensor.getType(),
                request.value());
        if (sensor.getType() == SensorType.CAMERA) {
            sensor.setActive(request.value() != 0);}
        SensorReading reading = readingRepo.save(
                new SensorReading(sensor, request.value())
        );
        return ReadingResponse.from(reading, alert);}

    @Transactional(readOnly = true)
    public List<ReadingResponse> listReadings(Long sensorId) {
        get(sensorId);
        return readingRepo
                .findBySensorIdOrderByCreatedAtDesc(sensorId)
                .stream()
                .map(reading -> ReadingResponse.from(
                        reading,
                        hasProblem(
                                reading.getSensor().getType(),
                                reading.getValue()
                        )
                ))
                .toList();}

    private boolean hasProblem(SensorType type, double value) {
        return switch (type) {
            case CAMERA -> value == 0;
            case LIGHTING -> value < MIN_LIGHT_PERCENT;
            case MOTION -> value == 1;};}

    private void validateReading(SensorType type, double value) {
        switch (type) {
            case LIGHTING -> {
                if (value < 0 || value > 100) {
                    throw new IllegalArgumentException(
                            "Luminosity have to be between 0 and 100.");}}

            case MOTION, CAMERA -> {
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException(
                            "Sensor value have to be 0 or 1.");}}}}

    private Sensor get(Long id) {
        return sensorRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Sensor", id));}
}
