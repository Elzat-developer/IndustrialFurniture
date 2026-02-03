package i.f.industrialfurniture.dto.admin;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EditProductDto(
        Integer productId,
        String productName,
        String description,
        String tag,//артикуль
        BigDecimal price,
        String material,
        String dimensions,
        Double weight,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer categoryId,
        Integer quantity,
        List<MultipartFile> photos
) {
}
