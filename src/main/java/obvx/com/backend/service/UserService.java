package obvx.com.backend.service;

import obvx.com.backend.dto.UserResponse;
import obvx.com.backend.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public UserResponse getCurrentUser(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}