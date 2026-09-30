package obvx.com.backend.controller;

import obvx.com.backend.dto.AdminRequest;
import obvx.com.backend.dto.UserResponse;
import obvx.com.backend.entity.Role;
import obvx.com.backend.exception.GlobalExceptionHandler;
import obvx.com.backend.service.AdminService;
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
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userResponse = new UserResponse(
                1L,
                "Admin User",
                "admin@obvx.com",
                Role.ADMIN
        );
    }

    @Test
    @DisplayName("POST /admin - Crée un administrateur et retourne 201 CREATED")
    void createAdmin_success() throws Exception {
        when(adminService.createAdmin(any(AdminRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Admin User",
                                    "email": "admin@obvx.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Admin User"))
                .andExpect(jsonPath("$.email").value("admin@obvx.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(adminService, times(1)).createAdmin(any(AdminRequest.class));
    }

    @Test
    @DisplayName("POST /admin - Retourne 400 BAD_REQUEST lorsque le mot de passe fait moins de 8 caractères")
    void createAdmin_invalidPassword_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Admin User",
                                    "email": "admin@obvx.com",
                                    "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Le mot de passe doit contenir au moins 8 caractères"));

        verify(adminService, never()).createAdmin(any());
    }

    @Test
    @DisplayName("POST /admin - Retourne 400 BAD_REQUEST lorsque l'email est invalide")
    void createAdmin_invalidEmail_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Admin User",
                                    "email": "not-an-email",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email invalide"));

        verify(adminService, never()).createAdmin(any());
    }
}
