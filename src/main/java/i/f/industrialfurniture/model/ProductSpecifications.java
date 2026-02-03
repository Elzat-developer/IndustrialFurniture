package i.f.industrialfurniture.model;

import i.f.industrialfurniture.model.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecifications {
    public static Specification<Product> hasCategory(Integer categoryId) {
        return (root, query, cb) -> categoryId == null ? null :
                cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasMaterial(String material) {
        return (root, query, cb) -> (material == null || material.isEmpty()) ? null :
                cb.like(root.get("material"), "%" + material + "%");
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("price"), min, max);
            return min != null ? cb.greaterThanOrEqualTo(root.get("price"), min) :
                    cb.lessThanOrEqualTo(root.get("price"), max);
        };
    }
}
