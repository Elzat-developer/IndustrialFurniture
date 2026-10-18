package i.f.industrialfurniture.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

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
                BigDecimal numeric = parseNumeric(trimmed);
                if (numeric != null && numeric.compareTo(BigDecimal.ZERO) > 0) {
                    value = numeric.stripTrailingZeros().toPlainString();
                } else {
                    value = CHECK_PRICE_VALUE;
                }
            }
        }
    }

    public boolean isNumeric() {
        return parseNumeric(value) != null && parseNumeric(value).compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getNumericPrice() {
        return parseNumeric(value);
    }

    public String getFormattedDisplay() {
        return formatPrice(value);
    }

    public static BigDecimal parseNumeric(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String clean = input.trim()
                .replace(" ", "")
                .replace("\u00A0", "")
                .replace("₸", "")
                .replace("KZT", "")
                .replace("kzt", "")
                .replace("руб", "")
                .replace("р.", "")
                .replace("тг", "")
                .replace("т", "")
                .replace("Т", "");
        if (clean.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(clean);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isNumericValue(String input) {
        BigDecimal num = parseNumeric(input);
        return num != null && num.compareTo(BigDecimal.ZERO) > 0;
    }

    public static String formatPrice(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return CHECK_PRICE_VALUE;
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("ru", "RU"));
        symbols.setGroupingSeparator(' ');
        DecimalFormat df = new DecimalFormat("#,##0", symbols);
        return df.format(amount) + " ₸";
    }

    public static String formatPrice(String input) {
        if (input == null || input.isBlank()) {
            return CHECK_PRICE_VALUE;
        }
        BigDecimal num = parseNumeric(input);
        if (num != null && num.compareTo(BigDecimal.ZERO) > 0) {
            return formatPrice(num);
        }
        String trimmed = input.trim();
        if (trimmed.equalsIgnoreCase("OUT_OF_STOCK") || trimmed.equalsIgnoreCase("out_of_stock")) {
            return OUT_OF_STOCK_VALUE;
        }
        if (trimmed.equalsIgnoreCase("ON_ORDER") || trimmed.equalsIgnoreCase("on_order")) {
            return ON_ORDER_VALUE;
        }
        if (trimmed.equalsIgnoreCase("CHECK_PRICE") || trimmed.equalsIgnoreCase("check_price")) {
            return CHECK_PRICE_VALUE;
        }
        return trimmed;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PriceProductStatus of(Object value) {
        if (value == null) {
            return CHECK_PRICE;
        }
        if (value instanceof PriceProductStatus pps) {
            return pps;
        }
        if (value instanceof java.util.Map<?, ?> map) {
            Object v = map.get("value");
            if (v == null) v = map.get("priceProductStatus");
            return of(v);
        }
        return new PriceProductStatus(String.valueOf(value));
    }

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

