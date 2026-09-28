package obvx.com.backend.service;

import obvx.com.backend.dto.UpdateUserRequest;
import obvx.com.backend.dto.UserResponse;
import obvx.com.backend.entity.User;
import obvx.com.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getCurrentUser(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public UserResponse updateCurrentUser(
            User user,
            UpdateUserRequest request
    ) {
        user.setName(request.name());
        user.setEmail(request.email());

        User updatedUser = userRepository.save(user);

        return new UserResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole()
        );
    }
}