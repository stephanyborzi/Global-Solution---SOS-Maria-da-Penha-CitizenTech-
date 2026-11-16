package com.citizentech.sos.service;

import com.citizentech.sos.dto.ContactDTO.*;
import com.citizentech.sos.exception.ResourceNotFoundException;
import com.citizentech.sos.model.Contact;
import com.citizentech.sos.repository.ContactRepository;
import com.citizentech.sos.security.AccessRules;
import com.citizentech.sos.security.AuthUser;
import com.citizentech.sos.security.InputSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável pelo gerenciamento dos contatos de confiança.
 * Implementa as operações de criação, consulta, atualização e remoção
 * dos contatos, garantindo que cada usuária acesse somente seus próprios dados.
 */

@Service
public class ContactService {
    private final ContactRepository contactRepo;
    private final UserService userService;

    public ContactService(ContactRepository contactRepo, UserService userService) {
        this.contactRepo = contactRepo;
        this.userService = userService;
    }

    public ContactResponse create(ContactCreateRequest r, AuthUser auth) {
        Contact contact = new Contact(userService.get(auth.id()), InputSanitizer.clean(r.name()), r.phone());
        return ContactResponse.from(contactRepo.save(contact));
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> listMine(AuthUser auth) {
        return contactRepo.findByUserId(auth.id()).stream().map(ContactResponse::from).toList();
    }

    @Transactional
    public ContactResponse update(Long id, ContactUpdateRequest r, AuthUser auth) {
        Contact contact = get(id);
        AccessRules.requireSelf(auth, contact.getUser().getId());
        contact.setName(InputSanitizer.clean(r.name()));
        contact.setPhone(r.phone());
        return ContactResponse.from(contactRepo.save(contact));
    }

    @Transactional
    public void remove(Long id, AuthUser auth) {
        Contact contact = get(id);
        AccessRules.requireSelf(auth, contact.getUser().getId());
        contactRepo.delete(contact);
    }

    private Contact get(Long id) {
        return contactRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contact", id));
    }
}
