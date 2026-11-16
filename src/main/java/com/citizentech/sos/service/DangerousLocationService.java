package com.citizentech.sos.service;

import com.citizentech.sos.dto.LocationDTO.*;
import com.citizentech.sos.exception.ResourceNotFoundException;
import com.citizentech.sos.model.DangerousLocation;
import com.citizentech.sos.repository.DangerousLocationRepository;
import com.citizentech.sos.security.AccessRules;
import com.citizentech.sos.security.AuthUser;
import com.citizentech.sos.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pelo gerenciamento de locais perigosos na plataforma CitizenTech.
 * Permite cadastrar locais informados pelos usuários, armazenando suas coordenadas geográficas.
 * Realiza a sanitização dos dados textuais para reduzir riscos de entradas maliciosas.
 * Disponibiliza a consulta dos locais perigosos que estão ativos na plataforma.
 * Controla a remoção de registros, verificando a propriedade do usuário ou permissão de administrador.
 * Utiliza o repositório para persistir, consultar e excluir os registros no banco de dados.
 * Contribui para o mapeamento de áreas de risco e a segurança dos cidadãos.
 */

@Service
public class DangerousLocationService {
    private final DangerousLocationRepository locationRepo;
    private final UserService userService;

    public DangerousLocationService(DangerousLocationRepository locationRepo, UserService userService) {
        this.locationRepo = locationRepo;
        this.userService = userService;
    }

    public LocationResponse create(LocationCreateRequest r, AuthUser auth) {
        DangerousLocation location = new DangerousLocation(userService.get(auth.id()),
                InputSanitizer.clean(r.type()), InputSanitizer.clean(r.description()),
                r.latitude(), r.longitude());
        return LocationResponse.from(locationRepo.save(location));
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> listActive() {
        return locationRepo.findByActiveTrue().stream().map(LocationResponse::from).toList();
    }

    @Transactional
    public void remove(Long id, AuthUser auth) {
        DangerousLocation location = locationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dangerous location", id));
        AccessRules.requireSelfOrAdmin(auth, location.getUser().getId());
        locationRepo.delete(location);
    }
}
