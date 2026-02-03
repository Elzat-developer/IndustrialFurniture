package i.f.industrialfurniture.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Позиция корзины")
public record CartItemDto(
        @Schema(description = "ID позиции", example = "1")
        Integer cart_item_id,

        @Schema(description = "ID товара", example = "22")
        Integer productId,

        @Schema(description = "Название товара", example = "Куртка кожаная")
        String productName,

        @Schema(description = "Количество", example = "2")
        Integer quantity,

        @Schema(description = "Цена", example = "7990")
        BigDecimal productPrice,
        @Schema(description = "Артикуль товара", example = "Куртка кожаная")
        String tag,
        @Schema(description = "Активность продута",example = "true")
        Boolean productActive
) {
}
