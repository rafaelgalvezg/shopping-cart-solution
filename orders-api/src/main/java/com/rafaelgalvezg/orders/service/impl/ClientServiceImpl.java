package com.rafaelgalvezg.orders.service.impl;

import com.rafaelgalvezg.orders.dto.ClientDto;
import com.rafaelgalvezg.orders.entity.Client;
import com.rafaelgalvezg.orders.exception.ResourceNotFoundException;
import com.rafaelgalvezg.orders.repository.ClientRepository;
import com.rafaelgalvezg.orders.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public Client createClient(String name, String email) {
        log.debug("Attempting to create or find client with email: {}", email);
        return clientRepository.findByEmail(email)
                .orElseGet(() -> {
                    Client newClient = new Client(name, email);
                    Client savedClient = clientRepository.save(newClient);
                    log.info("Created new client in database: ID={}, Name={}, Email={}",
                            savedClient.getId(), name, email);
                    return savedClient;
                });
    }

    @Override
    @Transactional
    public Client createClient(ClientDto clientDto) {
        log.debug("Attempting to create or find client from DTO with email: {}", clientDto.email());
        return clientRepository.findByEmail(clientDto.email())
                .orElseGet(() -> {
                    Client newClient = clientDto.toEntity();
                    Client savedClient = clientRepository.save(newClient);
                    log.info("Created new client from DTO: ID={}, Name={}, Email={}",
                            savedClient.getId(), clientDto.name(), clientDto.email());
                    return savedClient;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Client getClientById(Long id) {
        log.debug("Retrieving client with ID: {}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Client not found in database: ID={}", id);
                    return new ResourceNotFoundException("Client not found with ID: " + id);
                });
        log.info("Retrieved client from database: ID={}, Name={}, Email={}",
                client.getId(), client.getName(), client.getEmail());
        return client;
    }
}