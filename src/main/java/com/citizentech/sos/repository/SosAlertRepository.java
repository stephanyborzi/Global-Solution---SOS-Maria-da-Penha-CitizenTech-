package com.citizentech.sos.repository;

import com.citizentech.sos.model.SosAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SosAlertRepository extends JpaRepository<SosAlert, Long> {
    List<SosAlert> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<SosAlert> findAllByOrderByCreatedAtDesc();
}
