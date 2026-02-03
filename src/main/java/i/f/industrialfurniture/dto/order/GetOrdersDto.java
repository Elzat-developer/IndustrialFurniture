package i.f.industrialfurniture.dto.order;

import i.f.industrialfurniture.model.PaidStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GetOrdersDto(
        Integer orderId,
        String orderNumber,
        String customerName,
        String customerPhone,
        LocalDateTime orderStartDate,
        BigDecimal totalPrice,
        PaidStatus paidStatus
) {
}
