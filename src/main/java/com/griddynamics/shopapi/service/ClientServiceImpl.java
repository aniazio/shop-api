package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.ClientDto;
import com.griddynamics.shopapi.exception.ClientNotFoundException;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UserAlreadyExistsException;
import com.griddynamics.shopapi.exception.WrongCredentialsException;
import com.griddynamics.shopapi.model.Client;
import com.griddynamics.shopapi.model.ResetToken;
import com.griddynamics.shopapi.repository.ClientRepository;
import com.griddynamics.shopapi.repository.ResetTokenRepository;
import com.griddynamics.shopapi.security.Encoder;
import com.griddynamics.shopapi.util.PasswordRessetter;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {

  private final ClientRepository clientRepository;
  private final ResetTokenRepository tokenRepository;
  private final CartService cartService;
  private final PasswordRessetter passwordRessetter;

  public ClientServiceImpl(
      ClientRepository clientRepository,
      ResetTokenRepository tokenRepository,
      CartService cartService,
      PasswordRessetter passwordRessetter) {
    this.clientRepository = clientRepository;
    this.tokenRepository = tokenRepository;
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
    Client client = getClientFromDb(clientDto.getEmail());
    Optional<ResetToken> tokenFromDbOp = tokenRepository.findById(client.getId());
    if (tokenFromDbOp.isEmpty()) {
      throw new ForbiddenResourcesException(
          "Token is not present for client with id " + client.getId());
    }
    ResetToken tokenFromDb = tokenFromDbOp.get();
    if (tokenFromDb.isExpired()) {
      throw new ForbiddenResourcesException(
          "Token for client of id" + client.getId() + " is expired.");
    }
    client.encodeAndSetPassword(clientDto.getPasswordNotEncoded());
    clientRepository.save(client);
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
    Client client = getClientFromDb(clientDto.getEmail());
    ResetToken resetToken = new ResetToken();
    resetToken.setClient(client);
    ResetToken savedToken = tokenRepository.save(resetToken);
    passwordRessetter.sendEmailWithToken(clientDto.getEmail(), savedToken);
  }

  private Client getClientFromDb(String clientEmail) {
    Optional<Client> clientFromDb = clientRepository.findByEmail(clientEmail);
    if (clientFromDb.isEmpty()) {
      throw new ClientNotFoundException("Cart with email " + clientEmail + " not found");
    }
    return clientFromDb.get();
  }
}
