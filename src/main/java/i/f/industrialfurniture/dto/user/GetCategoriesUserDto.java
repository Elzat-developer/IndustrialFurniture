package i.f.industrialfurniture.dto.user;

import i.f.industrialfurniture.model.CategoryType;

public record GetCategoriesUserDto(
        Integer categoryId,
        String categoryName,
        String photoUrl,
        CategoryType categoryType
) {
}
