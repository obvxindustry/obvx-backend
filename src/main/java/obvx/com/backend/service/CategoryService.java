package obvx.com.backend.service;

import obvx.com.backend.dto.CategoryRequest;
import obvx.com.backend.entity.Category;
import obvx.com.backend.exception.RessourceNotFoundException;
import obvx.com.backend.repository.CategoryRepository;
import obvx.com.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;


    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public Category createCategory(CategoryRequest request) {

        Category category = new Category();

        category.setName(request.getName());

        return categoryRepository.save(category);
    }

    public List<Category> searchCategory(){
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Category not found"));
    }

    public Category updateCategory(Long id, CategoryRequest categoryRequest){
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RessourceNotFoundException("Catégorie introuvable")
                );

        existingCategory.setName(categoryRequest.getName());

        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RessourceNotFoundException("Catégorie introuvable")
                );

        long productCount = productRepository.countByCategoryId(id);

        if (productCount > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer cette catégorie car elle contient "
                            + productCount
                            + " produit(s)"
            );
        }

        categoryRepository.delete(category);
    }
}
