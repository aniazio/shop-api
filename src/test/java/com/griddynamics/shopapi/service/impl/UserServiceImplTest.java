package com.griddynamics.shopapi.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.UserDto;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UserAlreadyExistsException;
import com.griddynamics.shopapi.exception.WrongCredentialsException;
import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.repository.CartRepository;
import com.griddynamics.shopapi.repository.ResetTokenRepository;
import com.griddynamics.shopapi.repository.UserRepository;
import com.griddynamics.shopapi.util.Encoder;
import com.griddynamics.shopapi.util.PasswordRessetter;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock UserRepository userRepository;
  @Mock ResetTokenRepository resetTokenRepository;
  @Mock CartRepository cartRepository;
  @Mock PasswordRessetter passwordRessetter;
  @InjectMocks UserServiceImpl userService;
  @Captor ArgumentCaptor<User> captor;
  User user;
  String email = "some@mail.com";
  String passwordPlain = "123asb!";
  String passwordEncode = Encoder.encode(passwordPlain);
  long userId = 342L;
  UserDto userDto;
  ResetToken token;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(userId);
    user.setEmail(email);
    user.encodeAndSetPassword(passwordPlain);

    userDto = new UserDto();
    userDto.setEmail(email);
    userDto.setPassword(passwordPlain);

    token = new ResetToken();
    token.setUser(user);
    token.setId(3246L);
    token.setToken("dsklfjs-dsflk-xcvxcv");
  }

  @Test
  void shouldNot_register_when_alreadyExists() {
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    assertThrows(UserAlreadyExistsException.class, () -> userService.register(userDto));
  }

  @Test
  void should_register() {
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(userRepository.save(captor.capture())).thenReturn(null);

    userService.register(userDto);

    User captured = captor.getValue();

    assertEquals(email, captured.getEmail());
    assertTrue(Encoder.matches(passwordPlain, captured.getEncodedPassword()));
  }

  @Test
  void shouldNot_loginAndReturnCart_when_WrongPassword() {
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    userDto.setPassword("someother12*");

    assertThrows(WrongCredentialsException.class, () -> userService.loginAndReturnCart(userDto));
    then(userRepository).shouldHaveNoMoreInteractions();
    then(cartRepository).shouldHaveNoInteractions();
  }

  @Test
  void should_loginAndReturnCart_when_cartInDb() {
    Cart cart = new Cart();
    cart.setUser(user);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

    ArgumentCaptor<Cart> captorOrder = ArgumentCaptor.forClass(Cart.class);
    when(cartRepository.save(captorOrder.capture()))
        .thenAnswer(invocation -> invocation.getArguments()[0]);

    CartDto returned = userService.loginAndReturnCart(userDto);

    then(cartRepository).should().delete(cart);
    assertEquals(0, returned.getTotal());
    assertEquals(0, returned.getItems().size());
    assertEquals(userId, returned.getUserId());
  }

  @Test
  void should_loginAndReturnCart_when_noCartInDb() {
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

    ArgumentCaptor<Cart> captorOrder = ArgumentCaptor.forClass(Cart.class);
    when(cartRepository.save(captorOrder.capture()))
        .thenAnswer(invocation -> invocation.getArguments()[0]);

    CartDto returned = userService.loginAndReturnCart(userDto);

    then(cartRepository).shouldHaveNoMoreInteractions();
    assertEquals(0, returned.getTotal());
    assertEquals(0, returned.getItems().size());
    assertEquals(userId, returned.getUserId());
  }

  @Test
  void should_startResettingPassword_properRequest() {
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(resetTokenRepository.findById(userId)).thenReturn(Optional.of(token));
    when(resetTokenRepository.save(any())).thenReturn(token);
    ArgumentCaptor<ResetToken> captorToken = ArgumentCaptor.forClass(ResetToken.class);
    String answer = "some answer about resetting password";
    when(passwordRessetter.sendEmailWithToken(eq(email), captorToken.capture())).thenReturn(answer);

    String returned = userService.startResettingPassword(userDto);

    ResetToken captured = captorToken.getValue();

    then(resetTokenRepository).should().delete(token);
    assertEquals(answer, returned);
    assertEquals(userId, captured.getUser().getId());
    assertNotNull(captured.getToken());
    assertNotEquals(0, captured.getToken().length());
  }

  @Test
  void should_resetPassword_when_tokenNotExpired() {
    String newPassword = "opoi123)";
    userDto.setPassword(newPassword);
    token.setExpirationTime(LocalDateTime.now().plusMinutes(3));
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(resetTokenRepository.findById(userId)).thenReturn(Optional.of(token));
    when(userRepository.save(captor.capture())).thenReturn(null);

    userService.resetPassword(userDto, token.getToken());

    User captured = captor.getValue();
    then(resetTokenRepository).should().delete(token);
    assertTrue(Encoder.matches(newPassword, captured.getEncodedPassword()));
    assertEquals(userDto.getEmail(), captured.getEmail());
  }

  @Test
  void shouldNot_resetPassword_when_tokenExpired() {
    token.setExpirationTime(LocalDateTime.now().minusMinutes(3));
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(resetTokenRepository.findById(userId)).thenReturn(Optional.of(token));

    assertThrows(ForbiddenResourcesException.class, () -> userService.resetPassword(userDto, token.getToken()));

    then(userRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  void shouldNot_resetPassword_when_noTokenInDb() {
    token.setExpirationTime(LocalDateTime.now().minusMinutes(3));
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(resetTokenRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(ForbiddenResourcesException.class, () -> userService.resetPassword(userDto, token.getToken()));

    then(userRepository).shouldHaveNoMoreInteractions();
  }
}
