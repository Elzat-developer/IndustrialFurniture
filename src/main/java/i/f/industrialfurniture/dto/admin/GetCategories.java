package i.f.industrialfurniture.dto.admin;

import i.f.industrialfurniture.model.CategoryType;

public record GetCategories(
        Integer categoryId,
        String categoryName,
        String description,
        String photoUrl,
        CategoryType categoryType
) {
}
