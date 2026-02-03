package i.f.industrialfurniture.dto.user;

import java.math.BigDecimal;

public record GetProductsUserDto(
        Integer productId,
        String productName,
        BigDecimal productPrice,
        GetPhotoDto photoDtoList
) {
}
