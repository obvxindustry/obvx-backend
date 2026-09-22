package obvx.com.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import obvx.com.backend.entity.Category;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Category category;


    public ProductResponse(Long id, String name, String description, BigDecimal price, Integer stock, String imageUrl, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public ProductResponse(Long id, String name, String description, BigDecimal price, Integer stock, String imageUrl, Long categoryId, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.category = Category.builder().id(categoryId).name(categoryName).build();
    }
}
