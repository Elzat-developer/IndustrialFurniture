package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart,Integer> {
    Optional<Cart> findByCartToken(String cartToken);
}
