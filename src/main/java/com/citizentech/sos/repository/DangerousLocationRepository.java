package com.citizentech.sos.repository;

import com.citizentech.sos.model.DangerousLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DangerousLocationRepository extends JpaRepository<DangerousLocation, Long> {
    List<DangerousLocation> findByActiveTrue();
}
