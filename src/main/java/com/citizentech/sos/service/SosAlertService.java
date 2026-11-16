package com.citizentech.sos.service;

import com.citizentech.sos.dto.SosAlertDTO.*;
import com.citizentech.sos.exception.ResourceNotFoundException;
import com.citizentech.sos.model.SosAlert;
import com.citizentech.sos.repository.SosAlertRepository;
import com.citizentech.sos.security.AccessRules;
import com.citizentech.sos.security.AuthUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pelo gerenciamento dos alertas SOS da aplicação.
 * Realiza o registro de novas ocorrências com a localização geográfica da usuária,
 * consulta os alertas cadastrados e verifica as permissões de acesso aos registros.
 * Permite que administradores visualizem ocorrências de diferentes usuárias,
 * enquanto cidadãs podem acessar somente seus próprios alertas.
 * Gerencia o ciclo de vida das ocorrências por meio dos status OPEN (aberto),
 * IN_PROGRESS (em atendimento) e CLOSED (encerrado), permitindo acompanhar
 * a evolução de cada alerta desde o acionamento até a finalização.
 */

@Service
public class SosAlertService {
    private final SosAlertRepository alertRepo;
    private final UserService userService;

    public SosAlertService(SosAlertRepository alertRepo, UserService userService) {
        this.alertRepo = alertRepo;
        this.userService = userService;
    }

    public SosAlertResponse create(SosAlertCreateRequest r, AuthUser auth) {
        SosAlert alert = new SosAlert(userService.get(auth.id()), r.latitude(), r.longitude());
        return SosAlertResponse.from(alertRepo.save(alert));
    }

    @Transactional(readOnly = true)
    public List<SosAlertResponse> list(AuthUser auth, Long userIdFilter) {
        List<SosAlert> alerts;
        if (auth.isAdmin()) {
            alerts = userIdFilter == null
                    ? alertRepo.findAllByOrderByCreatedAtDesc()
                    : alertRepo.findByUserIdOrderByCreatedAtDesc(userIdFilter);
        } else {
            alerts = alertRepo.findByUserIdOrderByCreatedAtDesc(auth.id());
        }
        return alerts.stream().map(SosAlertResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SosAlertResponse getById(Long id, AuthUser auth) {
        SosAlert alert = get(id);
        AccessRules.requireSelfOrAdmin(auth, alert.getUser().getId());
        return SosAlertResponse.from(alert);
    }

    @Transactional
    public SosAlertResponse updateStatus(Long id, SosAlertStatusRequest r) {
        SosAlert alert = get(id);
        alert.setStatus(r.status());
        return SosAlertResponse.from(alertRepo.save(alert));
    }

    private SosAlert get(Long id) {
        return alertRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("SOS alert", id));
    }
}
