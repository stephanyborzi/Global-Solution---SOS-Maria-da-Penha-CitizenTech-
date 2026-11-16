package com.citizentech.sos.repository;

import com.citizentech.sos.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
