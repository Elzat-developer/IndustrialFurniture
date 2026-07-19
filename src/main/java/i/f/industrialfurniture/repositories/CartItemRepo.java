package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem,Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.product.id = :productId")
    void deleteByProductId(@Param("productId") Integer productId);

    // Запрос по ID работает быстрее и не триггерит ленивую загрузку прокси-объектов
    @Query("SELECT c FROM CartItem c WHERE c.cart.id = :cartId AND c.product.id = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Integer cartId, @Param("productId") Integer productId);
}
