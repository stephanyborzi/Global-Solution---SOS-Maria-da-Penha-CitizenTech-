package com.citizentech.sos.repository;

import com.citizentech.sos.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {}
