package obvx.com.backend.service;

import obvx.com.backend.entity.Role;
import obvx.com.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET);

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encodedSecret")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("generateToken - Génère un token JWT non vide et valide")
    void generateToken_success() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("extractEmail - Extrait correctement l'email du sujet du token")
    void extractEmail_success() {
        String token = jwtService.generateToken(user);

        String email = jwtService.extractEmail(token);

        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("isTokenValid - Retourne true quand le token correspond à l'utilisateur et n'est pas expiré")
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateToken(user);

        boolean isValid = jwtService.isTokenValid(token, user);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isTokenValid - Retourne false quand le token correspond à un autre utilisateur")
    void isTokenValid_differentUser_returnsFalse() {
        String token = jwtService.generateToken(user);

        User anotherUser = User.builder()
                .id(2L)
                .name("Other User")
                .email("other@example.com")
                .password("encodedSecret")
                .role(Role.CUSTOMER)
                .build();

        boolean isValid = jwtService.isTokenValid(token, anotherUser);

        assertThat(isValid).isFalse();
    }
}
