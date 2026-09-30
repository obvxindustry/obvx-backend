package obvx.com.backend.controller;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import obvx.com.backend.dto.ProductRequest;
import obvx.com.backend.dto.ProductResponse;
import obvx.com.backend.service.ProductsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductsService productsService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ProductsController(
            ProductsService productsService,
            ObjectMapper objectMapper,
            Validator validator
    ) {
        this.productsService = productsService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    // CREATE PRODUCT
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductResponse> createProducts(
            @RequestPart("products") String productsJson,
            @RequestPart("image") MultipartFile image
    ) throws IOException {

        ProductRequest productRequest =
                objectMapper.readValue(
                        productsJson,
                        ProductRequest.class
                );

        validateProductRequest(productRequest);

        ProductResponse productResponse =
                productsService.createProduct(
                        productRequest,
                        image
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productResponse);
    }

    // GET ALL PRODUCTS
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        return ResponseEntity.ok(
                productsService.getAllProducts()
        );
    }

    // GET PRODUCT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                productsService.searchProductById(id)
        );
    }

    // UPDATE PRODUCT
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestPart("products") String productsJson,
            @RequestPart(value = "image", required = false)
            MultipartFile image
    ) throws IOException {

        ProductRequest productRequest =
                objectMapper.readValue(
                        productsJson,
                        ProductRequest.class
                );

        validateProductRequest(productRequest);

        ProductResponse productResponse =
                productsService.updateProducts(
                        id,
                        productRequest,
                        image
                );

        return ResponseEntity.ok(productResponse);
    }

    // DELETE PRODUCT
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id
    ) {

        productsService.deleteProducts(id);

        return ResponseEntity.noContent().build();
    }

    // VALIDATION
    private void validateProductRequest(
            ProductRequest productRequest
    ) {

        var violations = validator.validate(productRequest);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}