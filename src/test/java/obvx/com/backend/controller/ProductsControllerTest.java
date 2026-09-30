package obvx.com.backend.controller;

import jakarta.validation.Validator;
import obvx.com.backend.dto.ProductRequest;
import obvx.com.backend.dto.ProductResponse;
import obvx.com.backend.entity.Category;
import obvx.com.backend.service.ProductsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductsControllerTest {

    @Mock
    private ProductsService productsService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Validator validator;

    @InjectMocks
    private ProductsController productsController;

    private MockMvc mockMvc;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productsController)
                .build();

        Category category = new Category();
        category.setId(1L);
        category.setName("Électronique");

        productResponse = new ProductResponse(
                1L,
                "iPhone 15",
                "Smartphone Apple",
                new BigDecimal("999.99"),
                50,
                "https://example.com/image.jpg",
                category
        );
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void createProducts_success() throws Exception {

        ProductRequest productRequest = new ProductRequest();

        productRequest.setName("iPhone 15");
        productRequest.setDescription("Smartphone Apple");
        productRequest.setPrice(new BigDecimal("999.99"));
        productRequest.setStock(50);
        productRequest.setCategoryId(1L);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "iphone.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes()
        );

        when(objectMapper.readValue(
                anyString(),
                eq(ProductRequest.class)
        )).thenReturn(productRequest);

        when(validator.validate(productRequest))
                .thenReturn(Collections.emptySet());

        when(productsService.createProduct(
                any(ProductRequest.class),
                any()
        )).thenReturn(productResponse);

        mockMvc.perform(
                        multipart("/products")
                                .file(image)
                                .file(
                                        new MockMultipartFile(
                                                "products",
                                                "",
                                                MediaType.APPLICATION_JSON_VALUE,
                                                "{}".getBytes()
                                        )
                                )
                                .contentType(
                                        MediaType.MULTIPART_FORM_DATA
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.description")
                        .value("Smartphone Apple"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.stock").value(50))
                .andExpect(jsonPath("$.imageUrl")
                        .value("https://example.com/image.jpg"));
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    void getAllProducts_success() throws Exception {

        when(productsService.getAllProducts())
                .thenReturn(List.of(productResponse));

        mockMvc.perform(
                        get("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("iPhone 15"))
                .andExpect(jsonPath("$[0].price").value(999.99))
                .andExpect(jsonPath("$[0].stock").value(50));
    }

    @Test
    void getAllProducts_empty() throws Exception {

        when(productsService.getAllProducts())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                        get("/products")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Test
    void getProductById_success() throws Exception {

        when(productsService.searchProductById(1L))
                .thenReturn(productResponse);

        mockMvc.perform(
                        get("/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.description")
                        .value("Smartphone Apple"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.stock").value(50));
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void updateProduct_success() throws Exception {

        ProductRequest productRequest = new ProductRequest();

        productRequest.setName("iPhone 15 Pro");
        productRequest.setDescription("Smartphone Apple Pro");
        productRequest.setPrice(new BigDecimal("1299.99"));
        productRequest.setStock(25);
        productRequest.setCategoryId(1L);

        ProductResponse updatedResponse = new ProductResponse(
                1L,
                "iPhone 15 Pro",
                "Smartphone Apple Pro",
                new BigDecimal("1299.99"),
                25,
                "https://example.com/new-image.jpg",
                productResponse.getCategory()
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "iphone-pro.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes()
        );

        when(objectMapper.readValue(
                anyString(),
                eq(ProductRequest.class)
        )).thenReturn(productRequest);

        when(validator.validate(productRequest))
                .thenReturn(Collections.emptySet());

        when(productsService.updateProducts(
                eq(1L),
                any(ProductRequest.class),
                any()
        )).thenReturn(updatedResponse);

        mockMvc.perform(
                        multipart("/products/1")
                                .file(image)
                                .file(
                                        new MockMultipartFile(
                                                "products",
                                                "",
                                                MediaType.APPLICATION_JSON_VALUE,
                                                "{}".getBytes()
                                        )
                                )
                                .with(request -> {
                                    request.setMethod("PUT");
                                    return request;
                                })
                                .contentType(
                                        MediaType.MULTIPART_FORM_DATA
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.description")
                        .value("Smartphone Apple Pro"))
                .andExpect(jsonPath("$.price").value(1299.99))
                .andExpect(jsonPath("$.stock").value(25));
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void deleteProduct_success() throws Exception {

        mockMvc.perform(
                        delete("/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }
}