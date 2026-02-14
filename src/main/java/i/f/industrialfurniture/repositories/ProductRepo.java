package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.ProductType;
import i.f.industrialfurniture.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product,Integer>, JpaSpecificationExecutor<Product> {
    List<Product> findAllByProductType(ProductType productType);
}
