package app.application.usecases;

import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.models.enums.UserStatus;
import app.domain.ports.IUserPort;
import app.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private IUserPort userPort;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthUseCase authUseCase;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setUsername("juanperez");
        activeUser.setPassword("$2a$10$hashedPassword");
        activeUser.setIdentificationId("12345678");
        activeUser.setName("Juan Pérez");
        activeUser.setSystemRole(SystemRole.INDIVIDUAL_CUSTOMER);
        activeUser.setUserStatus(UserStatus.ACTIVE);
    }

    // ── Test N-01 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Usuario ACTIVE con credenciales correctas → retorna token JWT")
    void login_activeUser_correctPassword_returnsToken() {
        when(userPort.findByUsername("juanperez")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("plainPassword", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("12345678", "juanperez", "INDIVIDUAL_CUSTOMER"))
                .thenReturn("jwt.token.generado");

        String token = authUseCase.login("juanperez", "plainPassword");

        assertThat(token).isEqualTo("jwt.token.generado");
        verify(jwtUtil).generateToken("12345678", "juanperez", "INDIVIDUAL_CUSTOMER");
    }

    // ── Test E-01 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Usuario ACTIVE con contraseña incorrecta → lanza IllegalArgumentException")
    void login_activeUser_wrongPassword_throwsException() {
        when(userPort.findByUsername("juanperez")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authUseCase.login("juanperez", "wrongPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválidos");

        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    // ── Test E-02 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Usuario no encontrado → lanza IllegalArgumentException con mensaje genérico")
    void login_userNotFound_throwsException() {
        when(userPort.findByUsername("desconocido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authUseCase.login("desconocido", "cualquierCosa"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválidos");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    // ── Test E-03 — VAL-05 del PROJECT_SPEC ───────────────────────────────────
    @Test
    @DisplayName("Usuario INACTIVE intenta login → lanza IllegalStateException")
    void login_inactiveUser_throwsIllegalStateException() {
        activeUser.setUserStatus(UserStatus.INACTIVE);
        when(userPort.findByUsername("juanperez")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("plainPassword", "$2a$10$hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authUseCase.login("juanperez", "plainPassword"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("INACTIVE");

        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    // ── Test E-04 — VAL-05 del PROJECT_SPEC ───────────────────────────────────
    @Test
    @DisplayName("Usuario BLOCKED intenta login → lanza IllegalStateException")
    void login_blockedUser_throwsIllegalStateException() {
        activeUser.setUserStatus(UserStatus.BLOCKED);
        when(userPort.findByUsername("juanperez")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("plainPassword", "$2a$10$hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authUseCase.login("juanperez", "plainPassword"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BLOCKED");

        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }
}
