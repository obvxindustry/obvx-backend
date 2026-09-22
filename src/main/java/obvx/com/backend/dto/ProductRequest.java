package obvx.com.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Name of Product is required")
    private String name;

    @NotNull(message = "ce champ est obligatoire")
    @Positive(message = "le montant doit être positive")
    private BigDecimal price;

    @NotNull(message = "ce champ est obligatoire")
    private String description;

    @NotNull(message = "ce champ est obligatoire")
    @PositiveOrZero(message = "Ce champs doit superieur ou égale à zero")
    private Integer stock;

    @NotNull(message = "image is required")
    private MultipartFile image;

    private String imageUrl;

    @NotNull(message = "Category is required")
    private long categoryId;
}