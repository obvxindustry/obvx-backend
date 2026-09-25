package obvx.com.backend.controller;

import obvx.com.backend.dto.UserResponse;
import obvx.com.backend.entity.User;
import obvx.com.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication
    ) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                userService.getCurrentUser(user)
        );
    }
}