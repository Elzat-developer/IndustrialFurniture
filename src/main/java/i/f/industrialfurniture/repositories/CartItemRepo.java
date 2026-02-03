package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.entity.Cart;
import i.f.industrialfurniture.model.entity.CartItem;
import i.f.industrialfurniture.model.entity.Product;
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

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
