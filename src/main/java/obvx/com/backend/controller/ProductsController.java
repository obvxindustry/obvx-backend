package obvx.com.backend.controller;

import jakarta.validation.Valid;
import obvx.com.backend.dto.ProductRequest;
import obvx.com.backend.dto.ProductResponse;
import obvx.com.backend.entity.Category;
import obvx.com.backend.entity.Products;
import obvx.com.backend.service.ProductsService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductsService productsService;

    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    /*
    //without DTO in project
    @PostMapping
    public ResponseEntity<Products> createProducts(@RequestBody Products products){
        Products createProducts = productsService.createProduct(products);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createProducts);
    }

    @GetMapping
    public ResponseEntity<List<Products>> searchProducts(){
        return ResponseEntity.ok(productsService.searchProduct());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Products> searchProductsById(@PathVariable Long id){
        return ResponseEntity.ok(productsService.searchProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Products> updateProducts(@PathVariable Long id,@RequestBody Products products){
        return ResponseEntity.ok(productsService.updateProducts(id, products));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducts(@PathVariable Long id){
        productsService.deleteProducts(id);

        return ResponseEntity.noContent().build();
    }

    */

    //with DTO
    @PostMapping
    public ResponseEntity<ProductResponse> createProducts(@Valid @RequestBody ProductRequest productRequest){
        ProductResponse productResponse = productsService.createProduct(productRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        return ResponseEntity.ok(
                productsService.getAllProducts()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(
                productsService.searchProductById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(
                productsService.updateProducts(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {

        productsService.deleteProducts(id);

        return ResponseEntity.noContent().build();
    }
}
