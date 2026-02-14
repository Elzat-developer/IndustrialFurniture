package i.f.industrialfurniture.dto.user;

import i.f.industrialfurniture.model.ProductType;

import java.math.BigDecimal;

public record GetProductsUserDto(
        Integer productId,
        String productName,
        BigDecimal productPrice,
        String material,
        Integer categoryId,
        ProductType productType,
        GetPhotoDto photoDtoList
) {
}
