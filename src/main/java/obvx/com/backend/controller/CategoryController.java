package obvx.com.backend.controller;

import jakarta.validation.Valid;
import obvx.com.backend.dto.CategoryRequest;
import obvx.com.backend.entity.Category;
import obvx.com.backend.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest categoryRequest){
        Category createCategory = categoryService.createCategory(categoryRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createCategory);
    }

    @GetMapping
    public ResponseEntity<List<Category>> searchCategory(){
        return ResponseEntity.ok(categoryService.searchCategory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> searchCategoryById(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest categoryRequest) {

        return ResponseEntity.ok(
                categoryService.updateCategory(id, categoryRequest)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }
}

