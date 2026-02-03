package i.f.industrialfurniture.dto.admin;

import org.springframework.web.multipart.MultipartFile;

public record CreateCategoryDto(
        String categoryName,
        String description,
        MultipartFile photoUrl
) {
}
