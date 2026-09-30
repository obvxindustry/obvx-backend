package obvx.com.backend.controller;

import obvx.com.backend.dto.OrderResponse;
import obvx.com.backend.entity.User;
import obvx.com.backend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(user));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                orderService.getMyOrders(user)
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrder(@PathVariable Long orderId, Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                orderService.getMyOrder(user, orderId)
        );
    }
}