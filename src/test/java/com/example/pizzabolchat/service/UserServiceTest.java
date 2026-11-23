package com.example.pizzabolchat.service;

import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.repository.AuthSessionRepository;
import com.example.pizzabolchat.repository.ProfileRepository;
import com.example.pizzabolchat.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private AuthSessionRepository authSessionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void changePasswordRejectsSamePassword() {
        User user = user(1L, "user@mail.ru", "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> userService.changePassword(1L, "secret", "secret"));

        assertEquals("Новый пароль совпадает с текущим", e.getMessage());
    }

    @Test
    void updateEmailRejectsTakenEmail() {
        User user = user(1L, "user@mail.ru", "hash");
        User other = user(2L, "taken@mail.ru", "hash2");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@mail.ru")).thenReturn(other);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> userService.updateEmail(1L, "taken@mail.ru"));

        assertEquals("Этот email уже занят", e.getMessage());
    }

    private User user(Long id, String email, String hash) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash(hash);
        return user;
    }
}
