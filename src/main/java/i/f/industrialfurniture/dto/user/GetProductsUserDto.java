package i.f.industrialfurniture.dto.user;

import i.f.industrialfurniture.model.PriceProductStatus;
import i.f.industrialfurniture.model.ProductType;

public record GetProductsUserDto(
        Integer productId,
        String productName,
        PriceProductStatus priceProductStatus,
        String material,
        Integer categoryId,
        ProductType productType,
        Boolean active,
        GetPhotoDto photoDtoList
) {
}
