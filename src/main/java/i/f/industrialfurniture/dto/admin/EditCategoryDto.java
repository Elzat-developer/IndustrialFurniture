package i.f.industrialfurniture.dto.admin;

import i.f.industrialfurniture.model.CategoryType;
import org.springframework.web.multipart.MultipartFile;

public record EditCategoryDto(
        Integer categoryId,
        String categoryName,
        String description,
        MultipartFile photoUrl,
        CategoryType categoryType,
        Boolean active
) {
}
