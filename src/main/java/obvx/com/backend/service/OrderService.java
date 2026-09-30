package obvx.com.backend.service;

import jakarta.transaction.Transactional;
import obvx.com.backend.dto.OrderItemResponse;
import obvx.com.backend.dto.OrderResponse;
import obvx.com.backend.entity.Cart;
import obvx.com.backend.entity.CartItem;
import obvx.com.backend.entity.Order;
import obvx.com.backend.entity.OrderItem;
import obvx.com.backend.entity.OrderStatus;
import obvx.com.backend.entity.Products;
import obvx.com.backend.entity.User;
import obvx.com.backend.exception.RessourceNotFoundException;
import obvx.com.backend.repository.CartItemRepository;
import obvx.com.backend.repository.CartRepository;
import obvx.com.backend.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public OrderResponse createOrder(User user) {

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new RessourceNotFoundException(
                                "Panier non trouvé"
                        )
                );

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException(
                    "Impossible de créer une commande avec un panier vide"
            );
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            Products product = cartItem.getProduct();

            int requestedQuantity = cartItem.getQuantity();

            if (product.getStock() < requestedQuantity) {
                throw new IllegalStateException(
                        "Stock insuffisant pour le produit : "
                                + product.getName()
                );
            }

            BigDecimal unitPrice = product.getPrice();

            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(requestedQuantity)
            );

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(requestedQuantity)
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(orderItem);

            product.setStock(
                    product.getStock() - requestedQuantity
            );

            total = total.add(subtotal);
        }

        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);

        cartItemRepository.deleteAllByCartId(cart.getId());

        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getMyOrders(User user) {

        return orderRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getMyOrder(User user, Long orderId) {

        Order order = orderRepository
                .findByIdAndUser(orderId, user)
                .orElseThrow(() ->
                        new RessourceNotFoundException(
                                "Commande non trouvée"
                        )
                );

        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::mapToItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt(),
                items
        );
    }

    private OrderItemResponse mapToItemResponse(OrderItem item) {

        Products product = item.getProduct();

        return new OrderItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}