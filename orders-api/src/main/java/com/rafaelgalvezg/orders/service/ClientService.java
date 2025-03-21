package com.rafaelgalvezg.orders.service;

import com.rafaelgalvezg.orders.dto.ClientDto;
import com.rafaelgalvezg.orders.entity.Client;

public interface ClientService {
    Client createClient(String name, String email);
    Client createClient(ClientDto clientDto);
    Client getClientById(Long id);
}