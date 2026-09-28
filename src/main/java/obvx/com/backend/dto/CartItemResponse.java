package obvx.com.backend.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal price,
        String imageUrl,
        Integer quantity,
        BigDecimal subtotal
) {}