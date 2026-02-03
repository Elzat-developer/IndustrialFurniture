package i.f.industrialfurniture.dto.order;

import i.f.industrialfurniture.model.PaidStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderHistoryUserDto(
        Integer orderId,
        String orderNumber,
        BigDecimal totalPrice,
        PaidStatus paidStatus,
        LocalDateTime createOrder,
        String whatsappLink
) {
}
