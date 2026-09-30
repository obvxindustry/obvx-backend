package obvx.com.backend.controller;

import obvx.com.backend.dto.UpdateUserRequest;
import obvx.com.backend.dto.UserResponse;
import obvx.com.backend.entity.Role;
import obvx.com.backend.entity.User;
import obvx.com.backend.exception.GlobalExceptionHandler;
import obvx.com.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;
    private Authentication authentication;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        user = User.builder()
                .id(1L)
                .name("Alice Smith")
                .email("alice@example.com")
                .role(Role.CUSTOMER)
                .build();

        authentication = new UsernamePasswordAuthenticationToken(user, null);

        userResponse = new UserResponse(
                1L,
                "Alice Smith",
                "alice@example.com",
                Role.CUSTOMER
        );
    }

    @Test
    @DisplayName("GET /users/me - Retourne les informations de l'utilisateur connecté avec statut 200 OK")
    void getCurrentUser_success() throws Exception {
        when(userService.getCurrentUser(any(User.class))).thenReturn(userResponse);

        mockMvc.perform(get("/users/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

        verify(userService, times(1)).getCurrentUser(any(User.class));
    }

    @Test
    @DisplayName("PUT /users/me - Met à jour les informations de l'utilisateur et retourne 200 OK")
    void updateCurrentUser_success() throws Exception {
        UserResponse updatedResponse = new UserResponse(
                1L,
                "Alice Updated",
                "alice.updated@example.com",
                Role.CUSTOMER
        );

        when(userService.updateCurrentUser(any(User.class), any(UpdateUserRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/users/me")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Alice Updated",
                                    "email": "alice.updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alice Updated"))
                .andExpect(jsonPath("$.email").value("alice.updated@example.com"));

        verify(userService, times(1)).updateCurrentUser(any(User.class), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /users/me - Retourne 400 BAD_REQUEST lorsque le nom est vide")
    void updateCurrentUser_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/users/me")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "email": "alice@example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Le nom est obligatoire"));

        verify(userService, never()).updateCurrentUser(any(), any());
    }

    @Test
    @DisplayName("PUT /users/me - Retourne 400 BAD_REQUEST lorsque l'email est invalide")
    void updateCurrentUser_invalidEmail_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/users/me")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Alice Smith",
                                    "email": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email invalide"));

        verify(userService, never()).updateCurrentUser(any(), any());
    }
}
