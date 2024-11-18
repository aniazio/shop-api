package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.UserDto;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UserAlreadyExistsException;
import com.griddynamics.shopapi.exception.WrongCredentialsException;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import com.griddynamics.shopapi.model.ResetToken;
import com.griddynamics.shopapi.model.User;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.repository.ResetTokenRepository;
import com.griddynamics.shopapi.repository.UserRepository;
import com.griddynamics.shopapi.service.UserService;
import com.griddynamics.shopapi.util.Encoder;
import com.griddynamics.shopapi.util.PasswordRessetter;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final ResetTokenRepository tokenRepository;
  private final OrderRepository orderRepository;
  private final PasswordRessetter passwordRessetter;

  @Override
  public void register(UserDto userDto) {
    Optional<User> userFromDb = userRepository.findByEmail(userDto.getEmail());
    if (userFromDb.isPresent()) {
      throw new UserAlreadyExistsException(
          String.format("There is already a user with email %s", userDto.getEmail()));
    }
    User user = new User();
    user.setEmail(userDto.getEmail());
    user.encodeAndSetPassword(userDto.getPassword());
    userRepository.save(user);
  }

  @Override
  public CartDto loginAndReturnCart(UserDto userDto) {
    User user = getUserFromDb(userDto.getEmail());
    if (!Encoder.matches(userDto.getPassword(), user.getEncodedPassword())) {
      throw new WrongCredentialsException("Wrong credentials: " + userDto);
    }
    Optional<OrderDetails> cartFromDb = orderRepository.findCartByUserId(user.getId());
    cartFromDb.ifPresent(orderRepository::delete);

    OrderDetails cart = new OrderDetails();
    cart.setUser(user);
    cart.setStatus(OrderStatus.CART);
    OrderDetails cartSaved = orderRepository.save(cart);
    return new CartDto(cartSaved);
  }

  @Override
  public String startResettingPassword(UserDto userDto) {
    User user = getUserFromDb(userDto.getEmail());

    Optional<ResetToken> tokenFromDb = tokenRepository.findById(user.getId());
    tokenFromDb.ifPresent(tokenRepository::delete);

    ResetToken resetToken = new ResetToken();
    resetToken.setUser(user);
    ResetToken savedToken = tokenRepository.save(resetToken);
    return passwordRessetter.sendEmailWithToken(userDto.getEmail(), savedToken);
  }

  @Override
  public void resetPassword(UserDto userDto, String token) {
    User user = getUserFromDb(userDto.getEmail());
    Optional<ResetToken> tokenFromDbOp = tokenRepository.findById(user.getId());
    if (tokenFromDbOp.isEmpty()) {
      throw new ForbiddenResourcesException(
          String.format("Token is not present for user with id %d", user.getId()));
    }
    ResetToken tokenFromDb = tokenFromDbOp.get();
    if (!token.equals(tokenFromDb.getToken()) || tokenFromDb.isExpired()) {
      throw new ForbiddenResourcesException(
          String.format("Token for user with id %d is expired", user.getId()));
    }
    tokenRepository.delete(tokenFromDb);
    user.encodeAndSetPassword(userDto.getPassword());
    userRepository.save(user);
  }

  private User getUserFromDb(String userEmail) {
    Optional<User> userFromDb = userRepository.findByEmail(userEmail);
    if (userFromDb.isEmpty()) {
      throw new WrongCredentialsException(String.format("User with email %s not found", userEmail));
    }
    return userFromDb.get();
  }
}
