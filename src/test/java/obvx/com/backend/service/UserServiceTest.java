package obvx.com.backend.service;

import obvx.com.backend.dto.UpdateUserRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Alice Smith")
                .email("alice@example.com")
                .password("encodedPassword")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("getCurrentUser - Retourne les informations de l'utilisateur connecté")
    void getCurrentUser_success() {
        UserResponse response = userService.getCurrentUser(user);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Alice Smith");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    @DisplayName("updateCurrentUser - Met à jour le profil de l'utilisateur avec succès")
    void updateCurrentUser_success() {
        UpdateUserRequest request = new UpdateUserRequest("Alice Updated", "alice.updated@example.com");

        User updatedSavedUser = User.builder()
                .id(1L)
                .name("Alice Updated")
                .email("alice.updated@example.com")
                .password("encodedPassword")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(updatedSavedUser);

        UserResponse response = userService.updateCurrentUser(user, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Alice Updated");
        assertThat(response.email()).isEqualTo("alice.updated@example.com");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);

        assertThat(user.getName()).isEqualTo("Alice Updated");
        assertThat(user.getEmail()).isEqualTo("alice.updated@example.com");
        verify(userRepository, times(1)).save(user);
    }
}
