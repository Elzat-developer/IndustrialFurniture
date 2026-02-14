package i.f.industrialfurniture.dto.admin;

import i.f.industrialfurniture.model.CategoryType;
import org.springframework.web.multipart.MultipartFile;

public record CreateCategoryDto(
        String categoryName,
        String description,
        MultipartFile photoUrl,
        CategoryType categoryType
) {
}
