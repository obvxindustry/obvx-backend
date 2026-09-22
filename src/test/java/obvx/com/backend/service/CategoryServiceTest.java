package obvx.com.backend.service;

import obvx.com.backend.dto.CategoryRequest;
import obvx.com.backend.entity.Category;
import obvx.com.backend.exception.RessourceNotFoundException;
import obvx.com.backend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Electronique")
                .build();

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Electronique");
    }

    @Test
    @DisplayName("createCategory - Succès lors de la création d'une catégorie")
    void createCategory_success() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.createCategory(categoryRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronique");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("searchCategory - Retourne la liste de toutes les catégories")
    void searchCategory_success() {
        Category category2 = Category.builder()
                .id(2L)
                .name("Mode")
                .build();

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category, category2));

        List<Category> result = categoryService.searchCategory();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Electronique");
        assertThat(result.get(1).getName()).isEqualTo("Mode");
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getCategoryById - Succès quand la catégorie existe")
    void getCategoryById_found() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronique");
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getCategoryById - Lève RessourceNotFoundException quand la catégorie n'existe pas")
    void getCategoryById_notFound_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Category not found");

        verify(categoryRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("updateCategory - Succès lors de la mise à jour d'une catégorie existante")
    void updateCategory_success() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Electronique & Informatique");

        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Electronique & Informatique")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        Category result = categoryService.updateCategory(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronique & Informatique");
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory - Lève RessourceNotFoundException quand la catégorie à modifier n'existe pas")
    void updateCategory_notFound_throwsException() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Nouveau Nom");

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, updateRequest))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Catégorie introuvable");

        verify(categoryRepository, times(1)).findById(99L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("deleteCategory - Succès lors de la suppression d'une catégorie existante")
    void deleteCategory_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    @DisplayName("deleteCategory - Lève RessourceNotFoundException quand la catégorie à supprimer n'existe pas")
    void deleteCategory_notFound_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(RessourceNotFoundException.class)
                .hasMessage("Catégorie introuvable");

        verify(categoryRepository, times(1)).findById(99L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
