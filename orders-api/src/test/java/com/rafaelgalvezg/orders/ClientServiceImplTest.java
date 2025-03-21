package com.rafaelgalvezg.orders;

import com.rafaelgalvezg.orders.dto.ClientDto;
import com.rafaelgalvezg.orders.entity.Client;
import com.rafaelgalvezg.orders.exception.ResourceNotFoundException;
import com.rafaelgalvezg.orders.repository.ClientRepository;
import com.rafaelgalvezg.orders.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    @DisplayName("Should create new client when email does not exist")
    void createClientWithNameAndEmail_success() {
        // Arrange
        String name = "Rafael Galvez";
        String email = "rafael@example.com";
        Client newClient = new Client(1L, name, email);
        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(newClient);

        // Act
        Client result = clientService.createClient(name, email);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(client -> {
                    assertThat(client.getId()).isEqualTo(1L);
                    assertThat(client.getName()).isEqualTo("Rafael Galvez");
                    assertThat(client.getEmail()).isEqualTo("rafael@example.com");
                });
        verify(clientRepository, times(1)).findByEmail(email);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Should return existing client when email already exists")
    void createClientWithNameAndEmail_existingClient() {
        // Arrange
        String name = "Rafael Galvez";
        String email = "rafael@example.com";
        Client existingClient = ClientTestFactory.createClient();
        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(existingClient));

        // Act
        Client result = clientService.createClient(name, email);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(client -> {
                    assertThat(client.getId()).isEqualTo(1L);
                    assertThat(client.getName()).isEqualTo("Rafael Galvez");
                    assertThat(client.getEmail()).isEqualTo("rafael@example.com");
                });
        verify(clientRepository, times(1)).findByEmail(email);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Should create new client from DTO when email does not exist")
    void createClientWithDto_success() {
        // Arrange
        ClientDto clientDto = ClientTestFactory.createClientDto();
        Client newClient = new Client(1L, clientDto.name(), clientDto.email());
        when(clientRepository.findByEmail(clientDto.email())).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(newClient);

        // Act
        Client result = clientService.createClient(clientDto);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(client -> {
                    assertThat(client.getId()).isEqualTo(1L);
                    assertThat(client.getName()).isEqualTo("Rafael Galvez");
                    assertThat(client.getEmail()).isEqualTo("rafael@example.com");
                });
        verify(clientRepository, times(1)).findByEmail(clientDto.email());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Should return existing client when DTO email already exists")
    void createClientWithDto_existingClient() {
        // Arrange
        ClientDto clientDto = ClientTestFactory.createClientDto();
        Client existingClient = ClientTestFactory.createClient();
        when(clientRepository.findByEmail(clientDto.email())).thenReturn(Optional.of(existingClient));

        // Act
        Client result = clientService.createClient(clientDto);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(client -> {
                    assertThat(client.getId()).isEqualTo(1L);
                    assertThat(client.getName()).isEqualTo("Rafael Galvez");
                    assertThat(client.getEmail()).isEqualTo("rafael@example.com");
                });
        verify(clientRepository, times(1)).findByEmail(clientDto.email());
        verify(clientRepository, never()).save(any(Client.class));
    }

    // --- getClientById ---
    @Test
    @DisplayName("Should return client when ID exists")
    void getClientById_success() {
        // Arrange
        Long id = 1L;
        Client client = ClientTestFactory.createClient();
        when(clientRepository.findById(id)).thenReturn(Optional.of(client));

        // Act
        Client result = clientService.getClientById(id);

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(c -> {
                    assertThat(c.getId()).isEqualTo(1L);
                    assertThat(c.getName()).isEqualTo("Rafael Galvez");
                    assertThat(c.getEmail()).isEqualTo("rafael@example.com");
                });
        verify(clientRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client ID does not exist")
    void getClientById_notFound_throwsResourceNotFoundException() {
        // Arrange
        Long id = 1L;
        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> clientService.getClientById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Client not found with ID: " + id);
        verify(clientRepository, times(1)).findById(id);
    }
}