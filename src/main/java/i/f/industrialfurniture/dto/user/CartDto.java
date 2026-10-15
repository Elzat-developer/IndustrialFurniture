package i.f.industrialfurniture.dto.user;

import i.f.industrialfurniture.model.PriceProductStatus;

import java.util.List;

public record CartDto(
        Integer cartId,
        List<CartItemDto> items,
        PriceProductStatus priceProductStatus,
        String totalPrice
) {
    public CartDto(Integer cartId, List<CartItemDto> items, String totalPrice) {
        this(cartId, items, PriceProductStatus.CHECK_PRICE, totalPrice != null ? totalPrice : PriceProductStatus.CHECK_PRICE_VALUE);
    }

    public CartDto(Integer cartId, List<CartItemDto> items) {
        this(cartId, items, PriceProductStatus.CHECK_PRICE, PriceProductStatus.CHECK_PRICE_VALUE);
    }
}