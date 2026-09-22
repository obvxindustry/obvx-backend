package obvx.com.backend.controller;

import obvx.com.backend.dto.CategoryRequest;
import obvx.com.backend.entity.Category;
import obvx.com.backend.exception.GlobalExceptionHandler;
import obvx.com.backend.exception.RessourceNotFoundException;
import obvx.com.backend.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private Category category;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        category = Category.builder()
                .id(1L)
                .name("Electronique")
                .build();
    }

    @Test
    @DisplayName("POST /category - Crée une catégorie et retourne 201 CREATED")
    void createCategory_success() throws Exception {
        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(category);

        mockMvc.perform(post("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Electronique\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronique"));

        verify(categoryService, times(1)).createCategory(any(CategoryRequest.class));
    }

    @Test
    @DisplayName("POST /category - Retourne 400 BAD_REQUEST lorsque le nom est vide")
    void createCategory_invalidName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(categoryService);
    }

    @Test
    @DisplayName("GET /category - Retourne 200 OK avec la liste des catégories")
    void searchCategory_success() throws Exception {
        Category category2 = Category.builder().id(2L).name("Livres").build();
        when(categoryService.searchCategory()).thenReturn(Arrays.asList(category, category2));

        mockMvc.perform(get("/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Electronique"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Livres"));

        verify(categoryService, times(1)).searchCategory();
    }

    @Test
    @DisplayName("GET /category/{id} - Retourne 200 OK quand la catégorie existe")
    void searchCategoryById_found() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronique"));

        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    @DisplayName("GET /category/{id} - Retourne 404 NOT_FOUND quand la catégorie n'existe pas")
    void searchCategoryById_notFound() throws Exception {
        when(categoryService.getCategoryById(99L))
                .thenThrow(new RessourceNotFoundException("Category not found"));

        mockMvc.perform(get("/category/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found"));

        verify(categoryService, times(1)).getCategoryById(99L);
    }

    @Test
    @DisplayName("PUT /category/{id} - Retourne 200 OK lors de la mise à jour")
    void updateCategory_success() throws Exception {
        Category updated = Category.builder().id(1L).name("Informatique").build();
        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/category/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Informatique\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Informatique"));

        verify(categoryService, times(1)).updateCategory(eq(1L), any(CategoryRequest.class));
    }

    @Test
    @DisplayName("DELETE /category/{id} - Retourne 204 NO_CONTENT")
    void deleteCategory_success() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/category/1"))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(1L);
    }
}
