package obvx.com.backend.dto;

import obvx.com.backend.entity.Role;

public record AuthResponse(
        Long id,
        String name,
        String email,
        Role role,
        String token
) {
}