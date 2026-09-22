package obvx.com.backend.repository;

import obvx.com.backend.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Products, Long> {

    long countByCategoryId(Long categoryId);
}
