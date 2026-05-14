package app.application;

import app.domain.models.IndividualCustomer;
import app.domain.models.enums.SystemRole;
import app.domain.ports.IUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterIndividualCustomerTest {

    @Mock
    private IUserPort userPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterIndividualCustomer registerIndividualCustomer;

    private IndividualCustomer validCustomer;

    @BeforeEach
    void setUp() {
        validCustomer = new IndividualCustomer();
        validCustomer.setName("María López");
        validCustomer.setIdentificationId("87654321");
        validCustomer.setEmail("maria@ejemplo.com");
        validCustomer.setPhone("3001234567");
        validCustomer.setAddress("Calle 123");
        validCustomer.setBirthDate(LocalDate.of(1995, 6, 15));
    }

    // ── Test N-01 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Registro exitoso → passwordEncoder.encode() fue llamado con la contraseña")
    void register_validCustomer_encodesPassword() {
        when(userPort.existsByIdentificationId("87654321")).thenReturn(false);
        when(passwordEncoder.encode("miPassword123")).thenReturn("$2a$10$hashed");

        IndividualCustomer result = registerIndividualCustomer.register(
                validCustomer, "mlopez", "miPassword123");

        verify(passwordEncoder).encode("miPassword123");
        verify(userPort).save(any());
        assertThat(result.getSystemRole()).isEqualTo(SystemRole.INDIVIDUAL_CUSTOMER);
    }

    // ── Test N-02 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Registro exitoso → rol asignado internamente como INDIVIDUAL_CUSTOMER")
    void register_validCustomer_assignsRoleInternally() {
        when(userPort.existsByIdentificationId("87654321")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashed");

        IndividualCustomer result = registerIndividualCustomer.register(
                validCustomer, "mlopez", "miPassword123");

        assertThat(result.getSystemRole()).isEqualTo(SystemRole.INDIVIDUAL_CUSTOMER);
    }

    // ── Test E-01 — VAL-03 del PROJECT_SPEC ───────────────────────────────────
    @Test
    @DisplayName("Email sin '@' → lanza IllegalArgumentException")
    void register_emailWithoutAt_throwsException() {
        validCustomer.setEmail("correo-invalido.com");

        assertThatThrownBy(() -> registerIndividualCustomer.register(
                validCustomer, "mlopez", "miPassword123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("correo");

        verify(passwordEncoder, never()).encode(any());
        verify(userPort, never()).save(any());
    }

    // ── Test E-02 — VAL-03 del PROJECT_SPEC ───────────────────────────────────
    @Test
    @DisplayName("Email sin dominio (sin '.') → lanza IllegalArgumentException")
    void register_emailWithoutDomain_throwsException() {
        validCustomer.setEmail("correo@sindominio");

        assertThatThrownBy(() -> registerIndividualCustomer.register(
                validCustomer, "mlopez", "miPassword123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("correo");

        verify(passwordEncoder, never()).encode(any());
        verify(userPort, never()).save(any());
    }

    // ── Test E-03 — VAL-01 del PROJECT_SPEC ───────────────────────────────────
    @Test
    @DisplayName("Cliente menor de 18 años → lanza IllegalArgumentException")
    void register_underageCustomer_throwsException() {
        validCustomer.setBirthDate(LocalDate.now().minusYears(17));

        assertThatThrownBy(() -> registerIndividualCustomer.register(
                validCustomer, "mlopez", "miPassword123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor de edad");

        verify(passwordEncoder, never()).encode(any());
        verify(userPort, never()).save(any());
    }

    // ── Test L-01 — EC-07 del PROJECT_SPEC ────────────────────────────────────
    @Test
    @DisplayName("Cliente que cumple exactamente 18 años hoy → registro aceptado")
    void register_exactlyEighteenToday_isAccepted() {
        validCustomer.setBirthDate(LocalDate.now().minusYears(18));
        when(userPort.existsByIdentificationId("87654321")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashed");

        IndividualCustomer result = registerIndividualCustomer.register(
                validCustomer, "mlopez", "miPassword123");

        assertThat(result).isNotNull();
        verify(userPort).save(any());
    }

    // ── Test E-04 — VAL-04 del PROJECT_SPEC ───────────────────────────────────
    @Test
    @DisplayName("Teléfono con 6 dígitos → lanza IllegalArgumentException")
    void register_phoneTooShort_throwsException() {
        validCustomer.setPhone("123456");

        assertThatThrownBy(() -> registerIndividualCustomer.register(
                validCustomer, "mlopez", "miPassword123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dígitos");

        verify(passwordEncoder, never()).encode(any());
        verify(userPort, never()).save(any());
    }

    // ── Test L-02 — EC-11 del PROJECT_SPEC ────────────────────────────────────
    @Test
    @DisplayName("Teléfono con exactamente 7 dígitos (límite mínimo) → registro aceptado")
    void register_phoneSevenDigits_isAccepted() {
        validCustomer.setPhone("1234567");
        when(userPort.existsByIdentificationId("87654321")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$10$hashed");

        IndividualCustomer result = registerIndividualCustomer.register(
                validCustomer, "mlopez", "miPassword123");

        assertThat(result).isNotNull();
        verify(userPort).save(any());
    }

    // ── Test E-05 — VAL-02 del PROJECT_SPEC ───────────────────────────────────
    @Test
    @DisplayName("Identificación duplicada → lanza IllegalArgumentException")
    void register_duplicateId_throwsException() {
        when(userPort.existsByIdentificationId("87654321")).thenReturn(true);

        assertThatThrownBy(() -> registerIndividualCustomer.register(
                validCustomer, "mlopez", "miPassword123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identificación");

        verify(passwordEncoder, never()).encode(any());
        verify(userPort, never()).save(any());
    }

    // ── Test E-06 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Nombre nulo → lanza IllegalArgumentException antes de cualquier operación")
    void register_nullName_throwsException() {
        validCustomer.setName(null);

        assertThatThrownBy(() -> registerIndividualCustomer.register(
                validCustomer, "mlopez", "miPassword123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");

        verify(userPort, never()).existsByIdentificationId(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userPort, never()).save(any());
    }

    // ── Test E-07 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Contraseña nula → lanza IllegalArgumentException")
    void register_nullPassword_throwsException() {
        assertThatThrownBy(() -> registerIndividualCustomer.register(
                validCustomer, "mlopez", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contraseña");

        verify(passwordEncoder, never()).encode(any());
        verify(userPort, never()).save(any());
    }
}
