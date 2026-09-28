package obvx.com.backend.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import obvx.com.backend.dto.AddToCartRequest;
import obvx.com.backend.dto.CartResponse;
import obvx.com.backend.dto.UpdateCartItemRequest;
import obvx.com.backend.entity.User;
import obvx.com.backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                cartService.getOrCreateCart(user)
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                cartService.addToCart(user, request)
        );
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(@PathVariable Long cartItemId, @Valid @RequestBody UpdateCartItemRequest request, Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        user,
                        cartItemId,
                        request
                )
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> deleteCartItem(
            @PathVariable Long cartItemId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                cartService.deleteCartItem(
                        user,
                        cartItemId
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                cartService.clearCart(user)
        );
    }
}