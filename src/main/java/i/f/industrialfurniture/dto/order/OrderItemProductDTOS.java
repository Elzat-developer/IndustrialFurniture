package i.f.industrialfurniture.dto.order;

import i.f.industrialfurniture.dto.user.GetPhotoDto;

import java.math.BigDecimal;

public record OrderItemProductDTOS(
        Integer productId,
        String productName,
        BigDecimal productPrice,
        GetPhotoDto photo,
        Boolean active
) {
}
