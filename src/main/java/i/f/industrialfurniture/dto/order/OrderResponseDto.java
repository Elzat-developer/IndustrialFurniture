package i.f.industrialfurniture.dto.order;

import java.math.BigDecimal;

public record OrderResponseDto(
        String orderNumber,
        String totalPrice,
        String whatsappLink
) {}
