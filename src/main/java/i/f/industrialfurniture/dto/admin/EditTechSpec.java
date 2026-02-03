package i.f.industrialfurniture.dto.admin;

import org.springframework.web.multipart.MultipartFile;

public record EditTechSpec(
        String fileName,
        Integer product_id,
        MultipartFile fileTechSpec
) {
}
