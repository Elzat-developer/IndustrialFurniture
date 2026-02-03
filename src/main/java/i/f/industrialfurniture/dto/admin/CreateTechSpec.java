package i.f.industrialfurniture.dto.admin;

import org.springframework.web.multipart.MultipartFile;

public record CreateTechSpec(
        String fileName,
        Integer product_id,
        MultipartFile fileTechSpec
) {
}
