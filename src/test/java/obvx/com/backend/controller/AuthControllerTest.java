package obvx.com.backend.controller;

import obvx.com.backend.dto.AuthResponse;
import obvx.com.backend.dto.LoginRequest;
import obvx.com.backend.dto.RegisterRequest;
import obvx.com.backend.entity.Role;
import obvx.com.backend.entity.User;
import obvx.com.backend.exception.GlobalExceptionHandler;
import obvx.com.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private User savedUser;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        savedUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedSecret")
                .role(Role.CUSTOMER)
                .build();

        authResponse = new AuthResponse(
                1L,
                "John Doe",
                "john@example.com",
                Role.CUSTOMER,
                "mocked.jwt.token"
        );
    }

    @Test
    @DisplayName("POST /auth/register - Inscription réussie et retourne 201 CREATED")
    void register_success() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(savedUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John Doe",
                                    "email": "john@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /auth/register - Retourne 400 BAD_REQUEST lorsque le mot de passe est trop court")
    void register_shortPassword_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John Doe",
                                    "email": "john@example.com",
                                    "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Le mot de passe doit contenir au moins 8 caractères"));

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("POST /auth/register - Retourne 400 BAD_REQUEST lorsque l'email est invalide")
    void register_invalidEmail_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "John Doe",
                                    "email": "invalid-email",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email invalide"));

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("POST /auth/login - Connexion réussie et retourne 200 OK avec token")
    void login_success() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "john@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - Retourne 400 BAD_REQUEST quand l'email est vide")
    void login_blankEmail_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(authService, never()).login(any());
    }
}
