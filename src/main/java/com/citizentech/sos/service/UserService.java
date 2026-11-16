package com.citizentech.sos.service;

import com.citizentech.sos.dto.UserDTO.*;
import com.citizentech.sos.exception.BusinessRuleException;
import com.citizentech.sos.exception.ConflictException;
import com.citizentech.sos.exception.ResourceNotFoundException;
import com.citizentech.sos.model.Profile;
import com.citizentech.sos.model.User;
import com.citizentech.sos.repository.ContactRepository;
import com.citizentech.sos.repository.UserRepository;
import com.citizentech.sos.security.AccessRules;
import com.citizentech.sos.security.AuthUser;
import com.citizentech.sos.security.InputSanitizer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pelo gerenciamento das usuárias do sistema SOS Mulher.
 * Centraliza as regras de negócio relacionadas ao cadastro, consulta,
 * atualização, alteração de senha e exclusão de usuários.
 * Também aplica regras de segurança para garantir que cada usuária
 * tenha acesso somente às informações que possui autorização para consultar
 * ou modificar.
 */

@Service
public class UserService {
    private final UserRepository userRepo;
    private final ContactRepository contactRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo, ContactRepository contactRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.contactRepo = contactRepo;
        this.encoder = encoder;
    }

    public UserResponse create(UserCreateRequest r) {
        String email = normalize(r.email());
        if (userRepo.existsByEmail(email)) throw new ConflictException("Email already registered");
        User user = new User(InputSanitizer.clean(r.name()), email, encoder.encode(r.password()), Profile.CITIZEN);
        return UserResponse.from(userRepo.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return userRepo.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id, AuthUser auth) {
        AccessRules.requireSelfOrAdmin(auth, id);
        return UserResponse.from(get(id));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest r, AuthUser auth) {
        AccessRules.requireSelfOrAdmin(auth, id);
        User user = get(id);
        String email = normalize(r.email());
        if (userRepo.existsByEmailAndIdNot(email, id)) throw new ConflictException("Email already registered");
        user.setName(InputSanitizer.clean(r.name()));
        user.setEmail(email);
        return UserResponse.from(userRepo.save(user));
    }

    @Transactional
    public void changePassword(Long id, UserPasswordRequest r, AuthUser auth) {
        AccessRules.requireSelf(auth, id);
        User user = get(id);
        if (!encoder.matches(r.currentPassword(), user.getPasswordHash()))
            throw new BusinessRuleException("Current password is incorrect");
        user.setPasswordHash(encoder.encode(r.newPassword()));
        userRepo.save(user);
    }

    @Transactional
    public void remove(Long id, AuthUser auth) {
        AccessRules.requireSelfOrAdmin(auth, id);
        User user = get(id);
        contactRepo.deleteByUserId(id);
        userRepo.delete(user);
        userRepo.flush();
    }

    public User get(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private String normalize(String email) { return email.trim().toLowerCase(); }
}
