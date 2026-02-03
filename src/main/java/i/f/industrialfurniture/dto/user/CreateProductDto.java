package i.f.industrialfurniture.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
         Integer quantity) {
}
