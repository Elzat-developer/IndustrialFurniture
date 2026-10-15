package i.f.industrialfurniture.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.List;

public record PriceProductStatus(
        @JsonValue
        String value
) implements Serializable {

    public static final String CHECK_PRICE_VALUE = "Уточните цену";
    public static final String OUT_OF_STOCK_VALUE = "Нет в наличии";
    public static final String ON_ORDER_VALUE = "Под заказ";

    public static final PriceProductStatus CHECK_PRICE = new PriceProductStatus(CHECK_PRICE_VALUE);
    public static final PriceProductStatus OUT_OF_STOCK = new PriceProductStatus(OUT_OF_STOCK_VALUE);
    public static final PriceProductStatus ON_ORDER = new PriceProductStatus(ON_ORDER_VALUE);

    public static final List<PriceProductStatus> ALL = List.of(CHECK_PRICE, OUT_OF_STOCK, ON_ORDER);
    public static final List<String> ALL_VALUES = List.of(CHECK_PRICE_VALUE, OUT_OF_STOCK_VALUE, ON_ORDER_VALUE);

    public PriceProductStatus {
        if (value == null || value.trim().isEmpty()) {
            value = CHECK_PRICE_VALUE;
        } else {
            String trimmed = value.trim();
            if (trimmed.equalsIgnoreCase("Нет в наличии")
                    || trimmed.equalsIgnoreCase("OUT_OF_STOCK")
                    || trimmed.equalsIgnoreCase("out_of_stock")
                    || trimmed.equalsIgnoreCase("Нет_в_наличии")
                    || trimmed.equalsIgnoreCase("Нет_в_наличий")) {
                value = OUT_OF_STOCK_VALUE;
            } else if (trimmed.equalsIgnoreCase("Под заказ")
                    || trimmed.equalsIgnoreCase("ON_ORDER")
                    || trimmed.equalsIgnoreCase("on_order")
                    || trimmed.equalsIgnoreCase("Под_заказ")) {
                value = ON_ORDER_VALUE;
            } else if (trimmed.equalsIgnoreCase("Уточните цену")
                    || trimmed.equalsIgnoreCase("CHECK_PRICE")
                    || trimmed.equalsIgnoreCase("check_price")
                    || trimmed.equalsIgnoreCase("Уточните_цену")) {
                value = CHECK_PRICE_VALUE;
            } else {
                value = CHECK_PRICE_VALUE;
            }
        }
    }

    @JsonCreator
    public static PriceProductStatus of(String value) {
        return new PriceProductStatus(value);
    }

    public static PriceProductStatus fromString(String value) {
        return new PriceProductStatus(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
