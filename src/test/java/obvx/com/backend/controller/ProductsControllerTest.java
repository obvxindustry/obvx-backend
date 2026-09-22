package obvx.com.backend.controller;

import obvx.com.backend.dto.ProductRequest;
import obvx.com.backend.dto.ProductResponse;
import obvx.com.backend.entity.Category;
import obvx.com.backend.exception.GlobalExceptionHandler;
import obvx.com.backend.exception.RessourceNotFoundException;
import obvx.com.backend.service.ProductsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductsService productsService;

    @InjectMocks
    private ProductsController productsController;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Category category = Category.builder().id(1L).name("Smartphones").build();

        productResponse = new ProductResponse(
                10L,
                "iPhone 15",
                "Smartphone Apple",
                new BigDecimal("999.99"),
                50,
                "https://supabase.co/img.jpg",
                category
        );
    }

    @Test
    @DisplayName("POST /products - Crée un produit avec image multipart et retourne 201 CREATED")
    void createProducts_success() throws Exception {
        when(productsService.createProduct(any(ProductRequest.class), any(MultipartFile.class)))
                .thenReturn(productResponse);

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "phone.png",
                "image/png",
                "fake image content".getBytes()
        );

        String productJson = "{"
                + "\"name\": \"iPhone 15\","
                + "\"description\": \"Smartphone Apple\","
                + "\"price\": 999.99,"
                + "\"stock\": 50,"
                + "\"categoryId\": 1"
                + "}";

        MockMultipartFile productPart = new MockMultipartFile(
                "products",
                "",
                "application/json",
                productJson.getBytes()
        );

        mockMvc.perform(multipart("/products")
                        .file(imagePart)
                        .file(productPart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.price").value(999.99));

        verify(productsService, times(1)).createProduct(any(ProductRequest.class), any(MultipartFile.class));
    }

    @Test
    @DisplayName("GET /products - Retourne 200 OK avec la liste des produits")
    void getAllProducts_success() throws Exception {
        when(productsService.getAllProducts()).thenReturn(Collections.singletonList(productResponse));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].name").value("iPhone 15"));

        verify(productsService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("GET /products/{id} - Retourne 200 OK quand le produit existe")
    void getProductById_found() throws Exception {
        when(productsService.searchProductById(10L)).thenReturn(productResponse);

        mockMvc.perform(get("/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("iPhone 15"));

        verify(productsService, times(1)).searchProductById(10L);
    }

    @Test
    @DisplayName("GET /products/{id} - Retourne 404 NOT_FOUND quand le produit n'existe pas")
    void getProductById_notFound() throws Exception {
        when(productsService.searchProductById(99L))
                .thenThrow(new RessourceNotFoundException("Product not found"));

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productsService, times(1)).searchProductById(99L);
    }

    @Test
    @DisplayName("DELETE /products/{id} - Retourne 204 NO_CONTENT")
    void deleteProduct_success() throws Exception {
        doNothing().when(productsService).deleteProducts(10L);

        mockMvc.perform(delete("/products/10"))
                .andExpect(status().isNoContent());

        verify(productsService, times(1)).deleteProducts(10L);
    }
}
