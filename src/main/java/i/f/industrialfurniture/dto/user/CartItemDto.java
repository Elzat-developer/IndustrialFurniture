package i.f.industrialfurniture.dto.user;

import i.f.industrialfurniture.model.PriceProductStatus;

public record CartItemDto(
        Integer cart_item_id,
        Integer productId,
        String productName,
        Integer quantity,
        PriceProductStatus priceProductStatus,
        String tag,
        Boolean productActive,
        String characteristics, // Новое: детальное описание
        GetPhotoDto photoDto,      // Новое: путь к файлу или URL изображения
        String deliveryTerms  // Новое: срок поставки (например, "3 рабочих дня")
) {
}
