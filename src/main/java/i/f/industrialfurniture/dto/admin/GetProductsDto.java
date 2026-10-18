package i.f.industrialfurniture.dto.admin;

import i.f.industrialfurniture.dto.user.GetPhotoDto;
import i.f.industrialfurniture.model.PriceProductStatus;
import i.f.industrialfurniture.model.ProductType;

import java.time.LocalDateTime;

public record GetProductsDto(
        Integer productId,
        String productName,
        String tag,//артикуль
        PriceProductStatus priceProductStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String material,
        Integer categoryId,
        ProductType productType,
        GetPhotoDto photoDto
) {
}
