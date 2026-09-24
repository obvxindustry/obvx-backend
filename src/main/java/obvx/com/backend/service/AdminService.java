package obvx.com.backend.service;

import obvx.com.backend.dto.AdminRequest;
import obvx.com.backend.dto.UserResponse;
import obvx.com.backend.entity.Role;
import obvx.com.backend.entity.User;
import obvx.com.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createAdmin(AdminRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Un utilisateur existe déjà avec cet email"
            );
        }

        User admin = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .build();

        User savedAdmin = userRepository.save(admin);

        return new UserResponse(
                savedAdmin.getId(),
                savedAdmin.getName(),
                savedAdmin.getEmail(),
                savedAdmin.getRole()
        );
    }
}