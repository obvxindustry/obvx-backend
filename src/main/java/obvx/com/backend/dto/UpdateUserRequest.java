package obvx.com.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String name,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        String email
) {}