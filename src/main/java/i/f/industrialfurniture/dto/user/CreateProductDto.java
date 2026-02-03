package i.f.industrialfurniture.dto.user;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateProductDto(
        String productName,
        String description,
        String tag,//артикуль
        BigDecimal price,
        String material,
        String dimensions,
        Double weight,
        LocalDateTime createdAt,
        Integer categoryId,
        Integer quantity,
        List<MultipartFile> photos) {
}
