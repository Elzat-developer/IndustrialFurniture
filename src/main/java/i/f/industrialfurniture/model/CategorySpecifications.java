package i.f.industrialfurniture.model;

import i.f.industrialfurniture.model.entity.Category;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecifications {
    public static Specification<Category> hasType(CategoryType categoryType) {
        return (root, query, cb) -> categoryType == null ? null :
                cb.equal(root.get("categoryType"), categoryType);
    }

    public static Specification<Category> hasActiveStatus(Boolean active) {
        return (root, query, cb) -> active == null ? null :
                cb.equal(root.get("active"), active);
    }
}
