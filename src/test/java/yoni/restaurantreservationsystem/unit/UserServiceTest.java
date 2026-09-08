package yoni.restaurantreservationsystem.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import yoni.restaurantreservationsystem.entities.User;
import yoni.restaurantreservationsystem.entities.UserType;
import yoni.restaurantreservationsystem.repositories.UserRepository;
import yoni.restaurantreservationsystem.services.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService.
 *
 * These tests verify password hashing during registration
 * and authentication behavior for valid and invalid passwords.
 *
 * The UserRepository is mocked, while a real BCryptPasswordEncoder
 * is used to verify the actual password hashing behavior.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerShouldHashPassword() {
        String rawPassword = "Password123";

        when(userRepository.existsByUsername("yoni"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.register(
                "yoni",
                rawPassword,
                UserType.CUSTOMER
        );

        assertNotEquals(rawPassword, savedUser.getPassword());

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        savedUser.getPassword()
                )
        );
    }

    @Test
    void loginShouldSucceedWithCorrectPassword() {
        String rawPassword = "Password123";

        User storedUser = new User(
                "yoni",
                passwordEncoder.encode(rawPassword),
                UserType.CUSTOMER
        );

        when(userRepository.findByUsername("yoni"))
                .thenReturn(Optional.of(storedUser));

        User loggedInUser = userService.login(
                "yoni",
                rawPassword
        );

        assertSame(storedUser, loggedInUser);
    }

    @Test
    void loginShouldFailWithIncorrectPassword() {
        String correctPassword = "Password123";

        User storedUser = new User(
                "yoni",
                passwordEncoder.encode(correctPassword),
                UserType.CUSTOMER
        );

        when(userRepository.findByUsername("yoni"))
                .thenReturn(Optional.of(storedUser));

        assertThrows(
                RuntimeException.class,
                () -> userService.login(
                        "yoni",
                        "WrongPassword"
                )
        );
    }
}