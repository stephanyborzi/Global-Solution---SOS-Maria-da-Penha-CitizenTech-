package com.citizentech.sos.service;

import com.citizentech.sos.dto.AuthDTO.*;
import com.citizentech.sos.dto.UserDTO.UserResponse;
import com.citizentech.sos.exception.UnauthorizedException;
import com.citizentech.sos.model.User;
import com.citizentech.sos.repository.UserRepository;
import com.citizentech.sos.security.JwtService;
import com.citizentech.sos.security.LoginAttemptService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pelo gerenciamento da autenticação dos usuários na plataforma CitizenTech.
 * Realiza o login por meio da validação do e-mail e da senha armazenada em hash.
 * Utiliza BCrypt para comparar as credenciais de forma segura.
 * Implementa controle de tentativas de login para reduzir ataques de força bruta.
 * Retorna mensagens genéricas para evitar a exposição de informações sobre contas existentes.
 * Após autenticação bem-sucedida, gera um token JWT para acesso aos recursos protegidos.
 * Também retorna informações do usuário e o tempo de expiração do token.
 */

@Service
public class AuthService {
    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final LoginAttemptService attempts;
    private final String dummyHash;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder,
                       JwtService jwtService, LoginAttemptService attempts) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.attempts = attempts;
        this.dummyHash = encoder.encode("dummy-password-for-timing");
    }

    public LoginResponse login(LoginRequest r) {
        String email = r.email().trim().toLowerCase();
        attempts.checkAllowed(email);

        User user = userRepo.findByEmail(email).orElse(null);
        String hash = user != null ? user.getPasswordHash() : dummyHash;
        boolean passwordMatches = encoder.matches(r.password(), hash);

        if (user == null || !passwordMatches) {
            attempts.recordFailure(email);
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
        attempts.reset(email);
        return new LoginResponse(jwtService.generate(user), "Bearer",
                jwtService.getExpirationMs(), UserResponse.from(user));
    }
}
