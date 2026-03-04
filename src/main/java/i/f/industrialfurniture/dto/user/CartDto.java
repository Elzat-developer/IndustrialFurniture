package i.f.industrialfurniture.dto.user;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(
        Integer cartId,
        List<CartItemDto> items,
        BigDecimal totalPrice
) {}