package obvx.com.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Name of Product is required")
    private String name;

    @NotNull(message = "ce champ est obligatoire")
    @Positive(message = "le montant doit être positif")
    private BigDecimal price;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "ce champ est obligatoire")
    @PositiveOrZero(message = "Ce champ doit être supérieur ou égal à zéro")
    private Integer stock;

    @NotNull(message = "Category is required")
    private Long categoryId;
}