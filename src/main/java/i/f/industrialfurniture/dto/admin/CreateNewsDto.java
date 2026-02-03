package i.f.industrialfurniture.dto.admin;

import org.springframework.web.multipart.MultipartFile;

public record CreateNewsDto(
        String name,
        String description,
        MultipartFile newsPhotoUrl
) {
}
