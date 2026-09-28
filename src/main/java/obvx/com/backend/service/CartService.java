package obvx.com.backend.service;

import jakarta.transaction.Transactional;
import obvx.com.backend.dto.AddToCartRequest;
import obvx.com.backend.dto.CartItemResponse;
import obvx.com.backend.dto.CartResponse;
import obvx.com.backend.dto.UpdateCartItemRequest;
import obvx.com.backend.entity.Cart;
import obvx.com.backend.entity.CartItem;
import obvx.com.backend.entity.Products;
import obvx.com.backend.entity.User;
import obvx.com.backend.exception.RessourceNotFoundException;
import obvx.com.backend.repository.CartItemRepository;
import obvx.com.backend.repository.CartRepository;
import obvx.com.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartResponse getOrCreateCart(User user) {

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(newCart);
                });

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::mapToCartItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                items,
                total
        );
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {

        BigDecimal subtotal = item.getProduct()
                .getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getProduct().getImageUrl(),
                item.getQuantity(),
                subtotal
        );
    }

    public CartResponse addToCart(User user, AddToCartRequest request) {

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(newCart);
                });

        Products product = productRepository.findById(request.productId())
                .orElseThrow(() ->
                        new RessourceNotFoundException("Produit non trouvé")
                );

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (cartItem != null) {

            cartItem.setQuantity(
                    cartItem.getQuantity() + request.quantity()
            );

        } else {

            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .build();

            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);

        return getOrCreateCart(user);
    }

    public CartResponse updateCartItem(User user, Long cartItemId, UpdateCartItemRequest request) {

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new RessourceNotFoundException("Panier non trouvé")
                );

        CartItem cartItem = cartItemRepository
                .findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() ->
                        new RessourceNotFoundException(
                                "Article du panier non trouvé"
                        )
                );

        cartItem.setQuantity(request.quantity());

        cartItemRepository.save(cartItem);

        return getOrCreateCart(user);
    }

    public CartResponse deleteCartItem(User user, Long cartItemId) {

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new RessourceNotFoundException("Panier non trouvé")
                );

        CartItem cartItem = cartItemRepository
                .findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() ->
                        new RessourceNotFoundException(
                                "Article du panier non trouvé"
                        )
                );

        cartItemRepository.delete(cartItem);

        return getOrCreateCart(user);
    }

    @Transactional
    public CartResponse clearCart(User user) {

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new RessourceNotFoundException("Panier non trouvé")
                );

        cartItemRepository.deleteAllByCartId(cart.getId());

        return getOrCreateCart(user);
    }
}