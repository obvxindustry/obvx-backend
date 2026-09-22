package obvx.com.backend.service;

import obvx.com.backend.dto.ProductRequest;
import obvx.com.backend.dto.ProductResponse;
import obvx.com.backend.entity.Category;
import obvx.com.backend.entity.Products;
import obvx.com.backend.exception.RessourceNotFoundException;
import obvx.com.backend.repository.CategoryRepository;
import obvx.com.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductsService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupabaseStorageService supabaseStorageService;

    public ProductsService(ProductRepository productRepository, CategoryRepository categoryRepository, SupabaseStorageService supabaseStorageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supabaseStorageService = supabaseStorageService;
    }

    public ProductResponse createProduct(
            ProductRequest productRequest,
            MultipartFile image) throws IOException {

        Category category = categoryRepository.findById(
                productRequest.getCategoryId()
        ).orElseThrow(() ->
                new RessourceNotFoundException("Catégorie introuvable")
        );

//        String imageUrl = null;
//
//        if (image != null && !image.isEmpty()) {
//            imageUrl = supabaseStorageService.uploadImage(image);
//        }

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("L'image du produit est obligatoire");
        }

        String imageUrl = supabaseStorageService.uploadImage(image);

        Products product = new Products();

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStock(productRequest.getStock());
        product.setImageUrl(imageUrl);
        product.setCategory(category);

        Products savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }



    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ProductResponse searchProductById(Long id) {

        Products product = productRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Product not found"));

        return mapToResponse(product);
    }

    public ProductResponse updateProducts(Long id, ProductRequest request) {

        Products product = productRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Product not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RessourceNotFoundException("Category not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        Products updatedProduct = productRepository.save(product);

        return mapToResponse(updatedProduct);
    }

    public void deleteProducts(Long id) {

        Products product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.delete(product);
    }


    private ProductResponse mapToResponse(Products product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }
}
