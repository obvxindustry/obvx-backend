package obvx.com.backend.controller;

import obvx.com.backend.dto.AddToCartRequest;
import obvx.com.backend.dto.CartItemResponse;
import obvx.com.backend.dto.CartResponse;
import obvx.com.backend.dto.UpdateCartItemRequest;
import obvx.com.backend.entity.Role;
import obvx.com.backend.entity.User;
import obvx.com.backend.exception.GlobalExceptionHandler;
import obvx.com.backend.exception.RessourceNotFoundException;
import obvx.com.backend.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private User user;
    private Authentication authentication;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cartController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.CUSTOMER)
                .build();

        authentication = new UsernamePasswordAuthenticationToken(user, null);

        CartItemResponse itemResponse = new CartItemResponse(
                100L,
                10L,
                "T-Shirt",
                new BigDecimal("25.00"),
                "https://example.com/item.png",
                2,
                new BigDecimal("50.00")
        );

        cartResponse = new CartResponse(
                1L,
                List.of(itemResponse),
                new BigDecimal("50.00")
        );
    }

    @Test
    @DisplayName("GET /cart - Retourne le panier de l'utilisateur avec statut 200 OK")
    void getCart_success() throws Exception {
        when(cartService.getOrCreateCart(any(User.class))).thenReturn(cartResponse);

        mockMvc.perform(get("/cart")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productName").value("T-Shirt"))
                .andExpect(jsonPath("$.total").value(50.00));

        verify(cartService, times(1)).getOrCreateCart(any(User.class));
    }

    @Test
    @DisplayName("POST /cart/items - Ajoute un produit au panier et retourne 200 OK")
    void addToCart_success() throws Exception {
        when(cartService.addToCart(any(User.class), any(AddToCartRequest.class))).thenReturn(cartResponse);

        mockMvc.perform(post("/cart/items")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "productId": 10,
                                    "quantity": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        verify(cartService, times(1)).addToCart(any(User.class), any(AddToCartRequest.class));
    }

    @Test
    @DisplayName("POST /cart/items - Retourne 400 BAD_REQUEST quand la quantité est inférieure ou égale à 0")
    void addToCart_invalidQuantity_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/cart/items")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "productId": 10,
                                    "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La quantité doit être supérieure à 0"));

        verify(cartService, never()).addToCart(any(), any());
    }

    @Test
    @DisplayName("POST /cart/items - Retourne 404 NOT_FOUND quand le produit n'existe pas")
    void addToCart_productNotFound_returnsNotFound() throws Exception {
        when(cartService.addToCart(any(User.class), any(AddToCartRequest.class)))
                .thenThrow(new RessourceNotFoundException("Produit non trouvé"));

        mockMvc.perform(post("/cart/items")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "productId": 999,
                                    "quantity": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Produit non trouvé"));

        verify(cartService, times(1)).addToCart(any(User.class), any(AddToCartRequest.class));
    }

    @Test
    @DisplayName("PUT /cart/items/{cartItemId} - Met à jour la quantité d'un article et retourne 200 OK")
    void updateCartItem_success() throws Exception {
        when(cartService.updateCartItem(any(User.class), eq(100L), any(UpdateCartItemRequest.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(put("/cart/items/100")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "quantity": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(cartService, times(1)).updateCartItem(any(User.class), eq(100L), any(UpdateCartItemRequest.class));
    }

    @Test
    @DisplayName("PUT /cart/items/{cartItemId} - Retourne 400 BAD_REQUEST quand la quantité est nulle")
    void updateCartItem_nullQuantity_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/cart/items/100")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La quantité est obligatoire"));

        verify(cartService, never()).updateCartItem(any(), any(), any());
    }

    @Test
    @DisplayName("DELETE /cart/items/{cartItemId} - Supprime un article du panier et retourne 200 OK")
    void deleteCartItem_success() throws Exception {
        CartResponse emptyCartResponse = new CartResponse(1L, List.of(), BigDecimal.ZERO);
        when(cartService.deleteCartItem(any(User.class), eq(100L))).thenReturn(emptyCartResponse);

        mockMvc.perform(delete("/cart/items/100")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items").isEmpty());

        verify(cartService, times(1)).deleteCartItem(any(User.class), eq(100L));
    }

    @Test
    @DisplayName("DELETE /cart - Vide complètement le panier et retourne 200 OK")
    void clearCart_success() throws Exception {
        CartResponse emptyCartResponse = new CartResponse(1L, List.of(), BigDecimal.ZERO);
        when(cartService.clearCart(any(User.class))).thenReturn(emptyCartResponse);

        mockMvc.perform(delete("/cart")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items").isEmpty());

        verify(cartService, times(1)).clearCart(any(User.class));
    }
}
