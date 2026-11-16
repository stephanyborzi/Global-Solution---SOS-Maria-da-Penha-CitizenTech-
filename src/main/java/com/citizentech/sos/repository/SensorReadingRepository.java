package com.citizentech.sos.repository;

import com.citizentech.sos.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findBySensorIdOrderByCreatedAtDesc(Long sensorId);
}
