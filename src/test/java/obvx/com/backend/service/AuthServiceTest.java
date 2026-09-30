package obvx.com.backend.service;

import obvx.com.backend.dto.AuthResponse;
import obvx.com.backend.dto.LoginRequest;
import obvx.com.backend.dto.RegisterRequest;
import obvx.com.backend.entity.Role;
import obvx.com.backend.entity.User;
import obvx.com.backend.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User customerUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        customerUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedSecret")
                .role(Role.CUSTOMER)
                .build();

        registerRequest = new RegisterRequest("John Doe", "john@example.com", "password123");
        loginRequest = new LoginRequest("john@example.com", "password123");
    }

    @Test
    @DisplayName("register - Succès lors de l'inscription d'un nouvel utilisateur")
    void register_success() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedSecret");
        when(userRepository.save(any(User.class))).thenReturn(customerUser);

        User result = authService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getRole()).isEqualTo(Role.CUSTOMER);

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register - Lève IllegalArgumentException quand l'email est déjà utilisé")
    void register_emailAlreadyExists_throwsIllegalArgumentException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Un utilisateur existe déjà avec cet email");

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login - Succès lors de la connexion avec identifiants valides")
    void login_success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(customerUser));
        when(passwordEncoder.matches("password123", "encodedSecret")).thenReturn(true);
        when(jwtService.generateToken(customerUser)).thenReturn("jwt.token.value");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
        assertThat(response.token()).isEqualTo("jwt.token.value");

        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedSecret");
        verify(jwtService, times(1)).generateToken(customerUser);
    }

    @Test
    @DisplayName("login - Lève IllegalArgumentException si l'utilisateur n'existe pas")
    void login_userNotFound_throwsIllegalArgumentException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email ou mot de passe incorrect");

        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("login - Lève IllegalArgumentException si le mot de passe est incorrect")
    void login_wrongPassword_throwsIllegalArgumentException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(customerUser));
        when(passwordEncoder.matches("password123", "encodedSecret")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email ou mot de passe incorrect");

        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(passwordEncoder, times(1)).matches("password123", "encodedSecret");
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("makeAdmin - Succès lors du passage d'un utilisateur au rôle ADMIN")
    void makeAdmin_success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(customerUser));
        when(userRepository.save(customerUser)).thenReturn(customerUser);

        authService.makeAdmin("john@example.com");

        assertThat(customerUser.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(userRepository, times(1)).save(customerUser);
    }

    @Test
    @DisplayName("makeAdmin - Lève IllegalArgumentException si l'utilisateur est introuvable")
    void makeAdmin_userNotFound_throwsIllegalArgumentException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.makeAdmin("unknown@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Utilisateur introuvable");

        verify(userRepository, times(1)).findByEmail("unknown@example.com");
        verify(userRepository, never()).save(any(User.class));
    }
}
