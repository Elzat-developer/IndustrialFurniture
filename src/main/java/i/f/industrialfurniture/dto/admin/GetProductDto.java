package i.f.industrialfurniture.dto.admin;

import i.f.industrialfurniture.dto.user.GetPhotoDto;
import i.f.industrialfurniture.model.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record GetProductDto(
        Integer productId,
        String productName,
        String description,
        String tag,//артикуль
        BigDecimal price,
        String material,
        String dimensions,
        Double weight,
        Integer width,  // Ширина, мм
        Integer depth,  // Глубина, мм
        Integer height, // Высота, мм
        String power,   // Мощность (может быть в кВт или ккал)
        String voltage, // Напряжение (220В, 380В)
        String country,
        Map<String, String> specifications,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer categoryId,
        Integer quantity,
        ProductType productType,
        List<GetPhotoDto> photos
) {
}
