package obvx.com.backend.service;

import obvx.com.backend.dto.AddToCartRequest;
import obvx.com.backend.dto.CartResponse;
import obvx.com.backend.dto.UpdateCartItemRequest;
import obvx.com.backend.entity.Cart;
import obvx.com.backend.entity.CartItem;
import obvx.com.backend.entity.Products;
import obvx.com.backend.entity.Role;
import obvx.com.backend.entity.User;
import obvx.com.backend.exception.RessourceNotFoundException;
import obvx.com.backend.repository.CartItemRepository;
import obvx.com.backend.repository.CartRepository;
import obvx.com.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private Products product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .build();

        product = Products.builder()
                .id(10L)
                .name("T-Shirt")
                .price(new BigDecimal("29.99"))
                .imageUrl("https://example.com/image.png")
                .build();

        cartItem = CartItem.builder()
                .id(100L)
                .product(product)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(user)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();

        cartItem.setCart(cart);
    }

    @Test
    @DisplayName("getOrCreateCart - Retourne le panier existant avec les articles et le montant total")
    void getOrCreateCart_existingCart_returnsCartResponse() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getOrCreateCart(user);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productName()).isEqualTo("T-Shirt");
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
        assertThat(response.items().get(0).subtotal()).isEqualByComparingTo(new BigDecimal("59.98"));
        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("59.98"));

        verify(cartRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("getOrCreateCart - Crée un nouveau panier vide si aucun panier n'existe")
    void getOrCreateCart_newCart_createsAndReturnsEmptyCart() {
        Cart emptyCart = Cart.builder()
                .id(2L)
                .user(user)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);

        CartResponse response = cartService.getOrCreateCart(user);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("addToCart - Ajoute un nouvel article au panier")
    void addToCart_newItem_success() {
        AddToCartRequest request = new AddToCartRequest(10L, 1);
        Cart emptyCart = Cart.builder()
                .id(1L)
                .user(user)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(emptyCart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addToCart(user, request);

        assertThat(response).isNotNull();
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addToCart - Incrémente la quantité si le produit est déjà dans le panier")
    void addToCart_existingItem_incrementsQuantity() {
        AddToCartRequest request = new AddToCartRequest(10L, 3);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        CartResponse response = cartService.addToCart(user, request);

        assertThat(response).isNotNull();
        assertThat(cartItem.getQuantity()).isEqualTo(5);
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    @DisplayName("addToCart - Lève RessourceNotFoundException si le produit n'existe pas")
    void addToCart_productNotFound_throwsException() {
        AddToCartRequest request = new AddToCartRequest(999L, 1);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(user, request))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Produit non trouvé");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCartItem - Met à jour la quantité d'un article du panier")
    void updateCartItem_success() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndCartId(100L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        CartResponse response = cartService.updateCartItem(user, 100L, request);

        assertThat(response).isNotNull();
        assertThat(cartItem.getQuantity()).isEqualTo(5);
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    @DisplayName("updateCartItem - Lève RessourceNotFoundException quand le panier n'existe pas")
    void updateCartItem_cartNotFound_throwsException() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateCartItem(user, 100L, request))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Panier non trouvé");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCartItem - Lève RessourceNotFoundException quand l'article n'existe pas dans le panier")
    void updateCartItem_itemNotFound_throwsException() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndCartId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateCartItem(user, 999L, request))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Article du panier non trouvé");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteCartItem - Supprime un article du panier avec succès")
    void deleteCartItem_success() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndCartId(100L, 1L)).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(cartItem);

        CartResponse response = cartService.deleteCartItem(user, 100L);

        assertThat(response).isNotNull();
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    @DisplayName("deleteCartItem - Lève RessourceNotFoundException quand le panier n'existe pas")
    void deleteCartItem_cartNotFound_throwsException() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteCartItem(user, 100L))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Panier non trouvé");

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteCartItem - Lève RessourceNotFoundException quand l'article n'existe pas")
    void deleteCartItem_itemNotFound_throwsException() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndCartId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteCartItem(user, 999L))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Article du panier non trouvé");

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("clearCart - Vide entièrement le panier de l'utilisateur")
    void clearCart_success() {

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        doNothing()
                .when(cartItemRepository)
                .deleteAllByCartId(1L);

        CartResponse response = cartService.clearCart(user);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.items()).isEmpty();
        assertThat(response.total())
                .isEqualByComparingTo(BigDecimal.ZERO);

        verify(cartItemRepository, times(1))
                .deleteAllByCartId(1L);
    }

    @Test
    @DisplayName("clearCart - Lève RessourceNotFoundException quand le panier n'existe pas")
    void clearCart_cartNotFound_throwsException() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.clearCart(user))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Panier non trouvé");

        verify(cartItemRepository, never()).deleteAllByCartId(any());
    }
}
