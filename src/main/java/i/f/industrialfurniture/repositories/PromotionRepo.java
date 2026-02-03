package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepo extends JpaRepository<Promotion,Integer> {
}
