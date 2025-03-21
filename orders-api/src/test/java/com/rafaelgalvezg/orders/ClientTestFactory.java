package com.rafaelgalvezg.orders;

import com.rafaelgalvezg.orders.dto.ClientDto;
import com.rafaelgalvezg.orders.entity.Client;

public class ClientTestFactory {

    public static Client createClient() {
        return new Client(1L, "Rafael Galvez", "rafael@example.com");
    }

    public static ClientDto createClientDto() {
        return new ClientDto(null, "Rafael Galvez", "rafael@example.com");
    }
}
