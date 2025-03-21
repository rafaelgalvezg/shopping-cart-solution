package com.rafaelgalvezg.orders.dto;

import com.rafaelgalvezg.orders.entity.Client;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientDto(
        Long id,

        @NotBlank(message = "Name cannot be blank")
        String name,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        String email
) {
    public Client toEntity() {
        return new Client(name, email);
    }

    public static ClientDto fromEntity(Client client) {
        return new ClientDto(client.getId(), client.getName(), client.getEmail());
    }
}