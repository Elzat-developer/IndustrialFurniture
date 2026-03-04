package i.f.industrialfurniture.dto.user;

import i.f.industrialfurniture.dto.order.OrderItemProductDTOS;
public record OrderItemDto(
        int quantity,
        OrderItemProductDTOS productInfo
) {
}
