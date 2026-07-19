package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.ProductType;
import i.f.industrialfurniture.model.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product,Integer>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = {"photos", "category"})
    List<Product> findAllByProductTypeAndActive(ProductType productType, Boolean active);

    // Добавлен EntityGraph для жадной загрузки фото, чтобы избежать N+1 на витрине
    @EntityGraph(attributePaths = {"photos"})
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
            "AND p.id != :productId " +
            "AND p.active = true " +
            "AND p.price BETWEEN :minPrice AND :maxPrice " +
            // Убрали RAND(). Лучше сортировать предсказуемо и быстро, а шафлить в Java.
            "ORDER BY CASE WHEN p.productType = :pType THEN 0 ELSE 1 END, p.createdAt DESC")
    List<Product> findSmartSimilar(
            @Param("categoryId") Integer categoryId,
            @Param("productId") Integer productId,
            @Param("pType") ProductType pType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @EntityGraph(attributePaths = {"photos"})
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
            "AND p.id != :productId " +
            // Важно: если excludeIds пустой, SQL упадет с синтаксической ошибкой (NOT IN ()).
            // В Service обязательно проверяй: if(excludeIds.isEmpty()) используй другой метод или передавай [-1]
            "AND p.id NOT IN :excludeIds " +
            "AND p.active = true " +
            "ORDER BY p.createdAt DESC")
    List<Product> findFallbackSimilar(
            @Param("categoryId") Integer categoryId,
            @Param("productId") Integer productId,
            @Param("excludeIds") List<Integer> excludeIds,
            Pageable pageable);
}
