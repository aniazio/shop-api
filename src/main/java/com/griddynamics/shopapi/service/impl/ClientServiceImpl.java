package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.ClientDto;
import com.griddynamics.shopapi.exception.ClientNotFoundException;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UserAlreadyExistsException;
import com.griddynamics.shopapi.exception.WrongCredentialsException;
import com.griddynamics.shopapi.model.Client;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import com.griddynamics.shopapi.model.ResetToken;
import com.griddynamics.shopapi.repository.ClientRepository;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.repository.ResetTokenRepository;
import com.griddynamics.shopapi.service.ClientService;
import com.griddynamics.shopapi.util.Encoder;
import com.griddynamics.shopapi.util.PasswordRessetter;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {

  private final ClientRepository clientRepository;
  private final ResetTokenRepository tokenRepository;
  private final OrderRepository orderRepository;
  private final PasswordRessetter passwordRessetter;

  public ClientServiceImpl(
      ClientRepository clientRepository,
      ResetTokenRepository tokenRepository,
      OrderRepository orderRepository,
      PasswordRessetter passwordRessetter) {
    this.clientRepository = clientRepository;
    this.tokenRepository = tokenRepository;
    this.orderRepository = orderRepository;
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
    client.encodeAndSetPassword(clientDto.getPassword());
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
    tokenRepository.delete(tokenFromDb);
    if (tokenFromDb.isExpired()) {
      throw new ForbiddenResourcesException(
          "Token for client of id" + client.getId() + " is expired.");
    }
    client.encodeAndSetPassword(clientDto.getPassword());
    clientRepository.save(client);
  }

  @Override
  public CartDto loginAndReturnCart(ClientDto clientDto) {
    Client client = getClientFromDb(clientDto.getEmail());
    if (!Encoder.matches(clientDto.getPassword(), client.getPassword())) {
      throw new WrongCredentialsException("Wrong credentials: " + clientDto);
    }
    Optional<OrderDetails> cartFromDb = orderRepository.findCartByClientId(client.getId());
    if (cartFromDb.isPresent()) {
      return new CartDto(cartFromDb.get());
    }
    OrderDetails cart = new OrderDetails();
    cart.setClient(client);
    cart.setStatus(OrderStatus.CART);
    OrderDetails cartSaved = orderRepository.save(cart);
    return new CartDto(cartSaved);
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
