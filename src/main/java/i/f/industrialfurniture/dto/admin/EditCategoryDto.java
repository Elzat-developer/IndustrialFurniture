package i.f.industrialfurniture.dto.admin;

import org.springframework.web.multipart.MultipartFile;

public record EditCategoryDto(
        Integer categoryId,
        String categoryName,
        String description,
        MultipartFile photoUrl
) {
}
