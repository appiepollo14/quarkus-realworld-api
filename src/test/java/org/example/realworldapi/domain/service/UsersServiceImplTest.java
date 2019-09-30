package org.example.realworldapi.domain.service;

import org.example.realworldapi.domain.builder.UserBuilder;
import org.example.realworldapi.domain.entity.persistent.User;
import org.example.realworldapi.domain.exception.EmailAlreadyExistsException;
import org.example.realworldapi.domain.exception.InvalidPasswordException;
import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.exception.UsernameAlreadyExistsException;
import org.example.realworldapi.domain.repository.UserRepository;
import org.example.realworldapi.domain.resource.service.UsersService;
import org.example.realworldapi.domain.resource.service.impl.UsersServiceImpl;
import org.example.realworldapi.domain.security.Role;
import org.example.realworldapi.util.UserUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsersServiceImplTest {

  private UserRepository userRepository;
  private JWTService jwtService;
  private UsersService usersService;

  @BeforeEach
  public void beforeEach() {
    userRepository = mock(UserRepository.class);
    jwtService = mock(JWTService.class);
    usersService = new UsersServiceImpl(userRepository, jwtService);
  }

  @Test
  public void givenValidNewUserData_thenReturnAnCreatedUserWithFilledTokenField() {

    String username = "user";
    String email = "user@email.com";
    String password = "user123";

    User createdUser = new User();
    createdUser.setId(1L);
    createdUser.setUsername(username);
    createdUser.setEmail(email);
    createdUser.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
    createdUser.setToken(UUID.randomUUID().toString());

    when(userRepository.create(any(User.class))).thenReturn(createdUser);
    when(jwtService.sign(createdUser.getId().toString(), Role.USER)).thenReturn("token");

    User resultUser = usersService.create(username, email, password);

    Assertions.assertNotNull(resultUser.getUsername());
    Assertions.assertNotNull(resultUser.getEmail());
    Assertions.assertNotNull(resultUser.getPassword());
    Assertions.assertNotNull(resultUser.getToken());
  }

  @Test
  public void whenExecuteCreateWithExistingEmail_shouldThrowsEmailAlreadyExistsException() {

    String username = "user";
    String email = "user@email.com";
    String password = "user123";

    when(userRepository.existsBy("email", email)).thenReturn(true);

    Assertions.assertThrows(
        EmailAlreadyExistsException.class, () -> usersService.create(username, email, password));
  }

  @Test
  public void whenExecuteCreateWithExistingUsername_shouldThrowsUsernameAlreadyExistsException() {

    String username = "user";
    String email = "user@email.com";
    String password = "user123";

    when(userRepository.existsBy("username", username)).thenReturn(true);

    Assertions.assertThrows(
        UsernameAlreadyExistsException.class, () -> usersService.create(username, email, password));
  }

  @Test
  public void givenAnValidLoginInfo_thenReturnsAUser() {

    String email = "user1@mail.com";
    String password = "123";

    Optional<User> existingUser = Optional.of(UserUtils.create(1L, "user1", email, password));

    when(userRepository.findByEmail(email)).thenReturn(existingUser);
    when(userRepository.update(existingUser.get())).thenReturn(existingUser.get());
    when(jwtService.sign(existingUser.get().getId().toString(), Role.USER)).thenReturn("token");

    User resultUser = usersService.login(email, password);

    Assertions.assertEquals(existingUser.get(), resultUser);
  }

  @Test
  public void givenAInvalidEmail_thenUserNotFoundException() {

    String email = "user1@mail.com";
    String password = "123";

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    Assertions.assertThrows(UserNotFoundException.class, () -> usersService.login(email, password));
  }

  @Test
  public void givenAValidEmailAndAInvalidPassword_thenThrowsInvalidPasswordException() {

    String email = "user1@mail.com";
    String password = "123";

    Optional<User> existingUser = Optional.of(UserUtils.create("user1", email, password));

    when(userRepository.findByEmail(email)).thenReturn(existingUser);

    Assertions.assertThrows(InvalidPasswordException.class, () -> usersService.login(email, "158"));
  }

  @Test
  public void givenAPersistedUser_whenExecuteFindById_shouldRetrieveUser() {

    User user = UserUtils.create("User1", "user1@mail.com", "user123");
    user.setId(1L);

    Optional<User> userResponse = Optional.of(user);

    when(userRepository.findById(user.getId())).thenReturn(userResponse);

    User result = usersService.findById(user.getId());

    Assertions.assertEquals(user.getId(), result.getId());
  }

  @Test
  public void givenANotPersistedUser_whenExecuteFindById_shouldThrowsUseNotFoundException() {

    Long userId = 1L;

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    Assertions.assertThrows(UserNotFoundException.class, () -> usersService.findById(userId));
  }

  @Test
  public void givenAExistentUser_whenExecuteUpdate_shouldReturnUpdatedUser() {

    User user =
        new UserBuilder().id(1L).username("user1").bio("user1 bio").email("user1@mail.com").build();

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    User result = usersService.update(user);

    Assertions.assertEquals(user.getEmail(), result.getEmail());
  }

  @Test
  public void givenAExistingUsername_shouldThrowsUsernameAlreadyExistsException() {

    User user =
        new UserBuilder().id(1L).username("user1").bio("user1 bio").email("user1@mail.com").build();

    when(userRepository.existsUsername(user.getId(), user.getUsername())).thenReturn(true);

    Assertions.assertThrows(UsernameAlreadyExistsException.class, () -> usersService.update(user));
  }

  @Test
  public void givenAExistingEmail_shouldThrowsEmailAlreadyExistsException() {

    User user =
        new UserBuilder().id(1L).username("user1").bio("user1 bio").email("user1@mail.com").build();

    when(userRepository.existsEmail(user.getId(), user.getEmail())).thenReturn(true);

    Assertions.assertThrows(EmailAlreadyExistsException.class, () -> usersService.update(user));
  }

  @Test
  public void givenAExistentUser_whenExecuteFindByUsername_shouldReturnAUser() {

    User user =
        new UserBuilder().id(1L).username("user1").bio("user1 bio").email("user1@mail.com").build();

    Optional<User> userOptional = Optional.of(user);

    when(userRepository.findByUsername(user.getUsername())).thenReturn(userOptional);

    User result = usersService.findByUsername(user.getUsername());

    Assertions.assertNotNull(result);
  }

  @Test
  public void givenAInexistentUser_whenExecuteFindByUsername_shouldThrowsUserNotFoundException() {

    String username = "user";

    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    Assertions.assertThrows(
        UserNotFoundException.class, () -> usersService.findByUsername(username));
  }
}
