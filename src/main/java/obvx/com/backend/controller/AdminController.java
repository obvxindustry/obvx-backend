package obvx.com.backend.controller;

import jakarta.validation.Valid;
import obvx.com.backend.dto.AdminRequest;
import obvx.com.backend.dto.UserResponse;
import obvx.com.backend.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createAdmin(
            @Valid @RequestBody AdminRequest request
    ) {
        UserResponse response = adminService.createAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}