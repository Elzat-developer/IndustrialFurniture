package i.f.industrialfurniture.dto.user;

import i.f.industrialfurniture.dto.order.OrderItemProductDTOS;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Товар в заказе")
public record OrderItemDto(
        @Schema(description = "Количество", example = "1")
        int quantity,
        OrderItemProductDTOS productInfo
) {
}
