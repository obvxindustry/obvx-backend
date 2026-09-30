package obvx.com.backend.service;

import obvx.com.backend.dto.AdminRequest;
import obvx.com.backend.dto.UserResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private AdminRequest adminRequest;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminRequest = new AdminRequest("Admin User", "admin@obvx.com", "password123");
        adminUser = User.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@obvx.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("createAdmin - Succès lors de la création d'un administrateur")
    void createAdmin_success() {
        when(userRepository.existsByEmail("admin@obvx.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        UserResponse response = adminService.createAdmin(adminRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Admin User");
        assertThat(response.email()).isEqualTo("admin@obvx.com");
        assertThat(response.role()).isEqualTo(Role.ADMIN);

        verify(userRepository, times(1)).existsByEmail("admin@obvx.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createAdmin - Lève IllegalArgumentException si l'email existe déjà")
    void createAdmin_emailAlreadyExists_throwsIllegalArgumentException() {
        when(userRepository.existsByEmail("admin@obvx.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createAdmin(adminRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Un utilisateur existe déjà avec cet email");

        verify(userRepository, times(1)).existsByEmail("admin@obvx.com");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }
}
