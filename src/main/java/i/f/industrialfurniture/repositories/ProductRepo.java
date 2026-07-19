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
    // EntityGraph говорит Hibernate достать photos и category сразу через SQL JOIN,
    // а не делать сотни мелких запросов потом.
    @EntityGraph(attributePaths = {"photos", "category"})
    List<Product> findAllByProductTypeAndActive(ProductType productType,Boolean active);
    // Основной запрос: Категория + Цена + Тип
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
            "AND p.id != :productId " +
            "AND p.active = true " +
            "AND p.price BETWEEN :minPrice AND :maxPrice " +
            "ORDER BY " +
            "CASE WHEN p.productType = :pType THEN 0 ELSE 1 END, " +
            "RAND()") // Для MySQL можно писать RAND() прямо в @Query в новых версиях Spring Data или FUNCTION('RAND')
    List<Product> findSmartSimilar(
            @Param("categoryId") Integer categoryId,
            @Param("productId") Integer productId,
            @Param("pType") ProductType pType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // Запасной запрос: Только категория (исключая уже найденные ID)
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId " +
            "AND p.id != :productId " +
            "AND p.id NOT IN :excludeIds " +
            "AND p.active = true " +
            "ORDER BY RAND()")
    List<Product> findFallbackSimilar(
            @Param("categoryId") Integer categoryId,
            @Param("productId") Integer productId,
            @Param("excludeIds") List<Integer> excludeIds,
            Pageable pageable);
}
