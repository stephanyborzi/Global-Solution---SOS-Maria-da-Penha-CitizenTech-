package com.citizentech.sos.dto;

import com.citizentech.sos.model.Contact;
import jakarta.validation.constraints.*;

public final class ContactDTO {
    private ContactDTO() {}

    private static final String PHONE_REGEX = "^\\+?[0-9]{10,15}$";

    public record ContactCreateRequest(
            @NotBlank String name,
            @NotBlank @Pattern(regexp = PHONE_REGEX, message = "invalid phone number") String phone) {}

    public record ContactUpdateRequest(
            @NotBlank String name,
            @NotBlank @Pattern(regexp = PHONE_REGEX, message = "invalid phone number") String phone) {}

    public record ContactResponse(Long id, Long userId, String name, String phone) {
        public static ContactResponse from(Contact c) {
            return new ContactResponse(c.getId(), c.getUser().getId(), c.getName(), c.getPhone());
        }
    }
}
