package i.f.industrialfurniture.model;

import i.f.industrialfurniture.model.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecifications {

    // Теперь статус динамический: true, false или null (все)
    public static Specification<Product> hasActiveStatus(Boolean active) {
        return (root, query, cb) -> active == null ? null :
                cb.equal(root.get("active"), active);
    }
    public static Specification<Product> hasCategory(Integer categoryId) {
        return (root, query, cb) -> categoryId == null ? null :
                cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasMaterial(String material) {
        return (root, query, cb) -> (material == null || material.isEmpty()) ? null :
                cb.equal(cb.lower(root.get("material")), material.toLowerCase());
    }

    public static Specification<Product> hasPriceStatus(String priceStatus) {
        return (root, query, cb) -> (priceStatus == null || priceStatus.isBlank()) ? null :
                cb.equal(cb.lower(root.get("price")), priceStatus.trim().toLowerCase());
    }

    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> null;
    }

    public static Specification<Product> hasType(ProductType productType) {
        return (root, query, cb) -> productType == null ? null :
                cb.equal(root.get("productType"), productType);
    }
}
