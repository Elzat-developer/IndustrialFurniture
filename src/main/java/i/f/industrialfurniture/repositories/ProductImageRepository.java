package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage,Integer> {
    // Находим все фото для списка ID продуктов
    List<ProductImage> findAllByProductIdIn(List<Integer> productIds);
}
