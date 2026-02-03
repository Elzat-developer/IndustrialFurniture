package i.f.industrialfurniture.dto.order;

import i.f.industrialfurniture.dto.user.OrderItemDto;
import i.f.industrialfurniture.model.PaidStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsDto(
        Integer orderId,
        String orderNumber,
        BigDecimal totalPrice,
        PaidStatus paidStatus,
        LocalDateTime createOrder,
        String whatsappLink,
        List<OrderItemDto> itemDto
) {
}
