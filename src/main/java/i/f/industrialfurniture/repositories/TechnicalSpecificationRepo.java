package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.entity.TechnicalSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechnicalSpecificationRepo extends JpaRepository<TechnicalSpecification,Integer> {
}
