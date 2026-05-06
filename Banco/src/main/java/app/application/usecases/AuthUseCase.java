package app.application.usecases;

import app.domain.models.User;
import app.domain.ports.IUserPort;
import app.infrastructure.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthUseCase {

    private final IUserPort userPort;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthUseCase(IUserPort userPort, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userPort = userPort;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String username, String password) {
        User user = userPort.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña inválidos"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Usuario o contraseña inválidos");
        }

        return jwtUtil.generateToken(
                user.getIdentificationId(),
                user.getUsername(),
                user.getSystemRole().name()
        );
    }
}