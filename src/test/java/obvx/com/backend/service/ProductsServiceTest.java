package obvx.com.backend.service;

import obvx.com.backend.dto.ProductRequest;
import obvx.com.backend.dto.ProductResponse;
import obvx.com.backend.entity.Category;
import obvx.com.backend.entity.Products;
import obvx.com.backend.exception.RessourceNotFoundException;
import obvx.com.backend.repository.CategoryRepository;
import obvx.com.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductsServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupabaseStorageService supabaseStorageService;

    @InjectMocks
    private ProductsService productsService;

    private Category category;
    private Products product;
    private ProductRequest productRequest;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Smartphones")
                .build();

        product = Products.builder()
                .id(10L)
                .name("iPhone 15")
                .description("Dernier iPhone")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .imageUrl("https://supabase.co/storage/v1/object/public/bucket/iphone15.jpg")
                .category(category)
                .build();

        productRequest = new ProductRequest();
        productRequest.setName("iPhone 15");
        productRequest.setDescription("Dernier iPhone");
        productRequest.setPrice(new BigDecimal("999.99"));
        productRequest.setStock(50);
        productRequest.setCategoryId(1L);

        mockFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Test
    @DisplayName("createProduct - Succès lors de la création d'un produit avec image")
    void createProduct_success() throws IOException {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(supabaseStorageService.uploadImage(mockFile))
                .thenReturn("https://supabase.co/storage/v1/object/public/bucket/iphone15.jpg");
        when(productRepository.save(any(Products.class))).thenReturn(product);

        ProductResponse response = productsService.createProduct(productRequest, mockFile);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("iPhone 15");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("999.99"));
        assertThat(response.getImageUrl()).contains("iphone15.jpg");
        assertThat(response.getCategory()).isNotNull();
        assertThat(response.getCategory().getId()).isEqualTo(1L);
        assertThat(response.getCategory().getName()).isEqualTo("Smartphones");

        verify(categoryRepository, times(1)).findById(1L);
        verify(supabaseStorageService, times(1)).uploadImage(mockFile);
        verify(productRepository, times(1)).save(any(Products.class));
    }

    @Test
    @DisplayName("createProduct - Lève RessourceNotFoundException si la catégorie n'existe pas")
    void createProduct_categoryNotFound_throwsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productsService.createProduct(productRequest, mockFile))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Catégorie introuvable");

        verify(categoryRepository, times(1)).findById(1L);
        verifyNoInteractions(supabaseStorageService);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct - Lève IllegalArgumentException si le fichier image est null")
    void createProduct_nullImage_throwsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> productsService.createProduct(productRequest, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("L'image du produit est obligatoire");

        verifyNoInteractions(supabaseStorageService);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProduct - Lève IllegalArgumentException si le fichier image est vide")
    void createProduct_emptyImage_throwsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        MockMultipartFile emptyFile = new MockMultipartFile("image", new byte[0]);

        assertThatThrownBy(() -> productsService.createProduct(productRequest, emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("L'image du produit est obligatoire");

        verifyNoInteractions(supabaseStorageService);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllProducts - Retourne la liste des produits mappés en ProductResponse")
    void getAllProducts_success() {
        Products product2 = Products.builder()
                .id(11L)
                .name("Samsung Galaxy S24")
                .description("Smartphone Samsung")
                .price(new BigDecimal("899.99"))
                .stock(30)
                .imageUrl("https://supabase.co/s24.jpg")
                .category(category)
                .build();

        when(productRepository.findAll()).thenReturn(Arrays.asList(product, product2));

        List<ProductResponse> result = productsService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("iPhone 15");
        assertThat(result.get(1).getName()).isEqualTo("Samsung Galaxy S24");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("searchProductById - Succès quand le produit existe")
    void searchProductById_found() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponse response = productsService.searchProductById(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("iPhone 15");
        verify(productRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("searchProductById - Lève RessourceNotFoundException quand le produit n'existe pas")
    void searchProductById_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productsService.searchProductById(99L))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Product not found");

        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("updateProducts - Succès de mise à jour avec nouvelle image")
    void updateProducts_withNewImage_success() throws IOException {
        Category newCategory = Category.builder().id(2L).name("Tablettes").build();
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("iPad Pro");
        updateRequest.setDescription("Mise à jour description");
        updateRequest.setPrice(new BigDecimal("1299.99"));
        updateRequest.setStock(20);
        updateRequest.setCategoryId(2L);

        Products updatedEntity = Products.builder()
                .id(10L)
                .name("iPad Pro")
                .description("Mise à jour description")
                .price(new BigDecimal("1299.99"))
                .stock(20)
                .imageUrl("https://supabase.co/new-ipad.jpg")
                .category(newCategory)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(supabaseStorageService.uploadImage(mockFile)).thenReturn("https://supabase.co/new-ipad.jpg");
        when(productRepository.save(any(Products.class))).thenReturn(updatedEntity);

        ProductResponse response = productsService.updateProducts(10L, updateRequest, mockFile);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("iPad Pro");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("1299.99"));
        assertThat(response.getImageUrl()).isEqualTo("https://supabase.co/new-ipad.jpg");
        assertThat(response.getCategory().getId()).isEqualTo(2L);

        verify(supabaseStorageService, times(1)).uploadImage(mockFile);
        verify(productRepository, times(1)).save(any(Products.class));
    }

    @Test
    @DisplayName("updateProducts - Succès de mise à jour sans nouvelle image (garde l'ancienne)")
    void updateProducts_withoutNewImage_retainsOldImage() throws IOException {
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("iPhone 15 Pro");
        updateRequest.setDescription("Description modifiée");
        updateRequest.setPrice(new BigDecimal("1099.99"));
        updateRequest.setStock(40);
        updateRequest.setCategoryId(1L);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Products.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productsService.updateProducts(10L, updateRequest, null);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(response.getImageUrl()).isEqualTo("https://supabase.co/storage/v1/object/public/bucket/iphone15.jpg");

        verifyNoInteractions(supabaseStorageService);
        verify(productRepository, times(1)).save(any(Products.class));
    }

    @Test
    @DisplayName("updateProducts - Lève RessourceNotFoundException si le produit n'existe pas")
    void updateProducts_productNotFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productsService.updateProducts(99L, productRequest, mockFile))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Product not found");

        verify(productRepository, times(1)).findById(99L);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProducts - Lève RessourceNotFoundException si la catégorie n'existe pas")
    void updateProducts_categoryNotFound_throwsException() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productsService.updateProducts(10L, productRequest, mockFile))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Category not found");

        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteProducts - Succès lors de la suppression d'un produit")
    void deleteProducts_success() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productsService.deleteProducts(10L);

        verify(productRepository, times(1)).findById(10L);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("deleteProducts - Lève RessourceNotFoundException si le produit n'existe pas")
    void deleteProducts_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productsService.deleteProducts(99L))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Product not found");

        verify(productRepository, times(1)).findById(99L);
        verify(productRepository, never()).delete(any());
    }
}
