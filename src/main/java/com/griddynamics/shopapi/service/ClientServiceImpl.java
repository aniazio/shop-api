package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.ClientDto;
import com.griddynamics.shopapi.exception.ClientNotFoundException;
import com.griddynamics.shopapi.exception.UserAlreadyExistsException;
import com.griddynamics.shopapi.exception.WrongCredentialsException;
import com.griddynamics.shopapi.model.Client;
import com.griddynamics.shopapi.repository.ClientRepository;
import com.griddynamics.shopapi.security.Encoder;
import com.griddynamics.shopapi.util.PasswordRessetter;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {

  private final ClientRepository clientRepository;
  private final CartService cartService;
  private final PasswordRessetter passwordRessetter;

  public ClientServiceImpl(
      ClientRepository clientRepository,
      CartService cartService,
      PasswordRessetter passwordRessetter) {
    this.clientRepository = clientRepository;
    this.cartService = cartService;
    this.passwordRessetter = passwordRessetter;
  }

  @Override
  public void register(ClientDto clientDto) {
    Optional<Client> clientFromDb = clientRepository.findByEmail(clientDto.getEmail());
    if (clientFromDb.isPresent()) {
      throw new UserAlreadyExistsException(
          "There is already a user with email " + clientDto.getEmail());
    }
    Client client = new Client();
    client.setEmail(clientDto.getEmail());
    client.encodeAndSetPassword(clientDto.getPasswordNotEncoded());
    clientRepository.save(client);
  }

  @Override
  public void resetPassword(ClientDto clientDto, String token) {
    // TODO implement resetting password - repo with tokens and expiration times
  }

  @Override
  public CartDto loginAndReturnCart(ClientDto clientDto) {
    Client client = getClientFromDb(clientDto.getEmail());
    if (!Encoder.matches(clientDto.getPasswordNotEncoded(), client.getPassword())) {
      throw new WrongCredentialsException("Wrong credentials: " + clientDto);
    }
    return cartService.getCartFor(client.getId());
  }

  @Override
  public void startResettingPassword(ClientDto clientDto) {
    getClientFromDb(clientDto.getEmail());
    passwordRessetter.sendEmailWithToken(clientDto.getEmail());
  }

  private Client getClientFromDb(String clientEmail) {
    Optional<Client> clientFromDb = clientRepository.findByEmail(clientEmail);
    if (clientFromDb.isEmpty()) {
      throw new ClientNotFoundException("Cart with email " + clientEmail + " not found");
    }
    return clientFromDb.get();
  }
}
