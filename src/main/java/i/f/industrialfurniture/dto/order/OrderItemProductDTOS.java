package i.f.industrialfurniture.dto.order;

import i.f.industrialfurniture.dto.user.GetPhotoDto;
import i.f.industrialfurniture.model.PriceProductStatus;

public record OrderItemProductDTOS(
        Integer productId,
        String productName,
        PriceProductStatus priceProductStatus,
        GetPhotoDto photo,
        Boolean active
) {
}
