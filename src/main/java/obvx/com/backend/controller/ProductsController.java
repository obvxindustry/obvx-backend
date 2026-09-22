package obvx.com.backend.controller;

import jakarta.validation.Valid;
import obvx.com.backend.dto.ProductRequest;
import obvx.com.backend.dto.ProductResponse;
import obvx.com.backend.service.ProductsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductsService productsService;

    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    // CREATE PRODUCT
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ProductResponse> createProducts(
//            @RequestPart("products") @Valid ProductRequest productRequest,
//            @RequestPart("image") MultipartFile image
//    ) throws IOException {
//
//        ProductResponse productResponse =
//                productsService.createProduct(productRequest, image);
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(productResponse);
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProducts(
            @RequestPart("products") String productsJson,
            @RequestPart("image") MultipartFile image
    ) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        ProductRequest productRequest =
                objectMapper.readValue(productsJson, ProductRequest.class);

        ProductResponse productResponse =
                productsService.createProduct(productRequest, image);

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
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ProductResponse> updateProduct( @PathVariable Long id, @RequestPart("products") @Valid ProductRequest request, @RequestPart(value = "image", required = false) MultipartFile image ) throws IOException {
        return ResponseEntity.ok( productsService.updateProducts(id, request, image) );
    }

    // DELETE PRODUCT
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id
    ) {

        productsService.deleteProducts(id);

        return ResponseEntity.noContent().build();
    }
}

