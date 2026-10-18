package i.f.industrialfurniture.dto.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import i.f.industrialfurniture.model.PriceProductStatus;
import i.f.industrialfurniture.model.ProductType;

import java.time.LocalDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateProductDto(
        String productName,
        String description,
        String tag, // артикул
        @JsonAlias({"price", "productPrice"})
        PriceProductStatus priceProductStatus,
        String material,
        String dimensions,
        Double weight,
        Integer width,  // Ширина, мм
        Integer depth,  // Глубина, мм
        Integer height, // Высота, мм
        String power,   // Мощность (может быть в кВт или ккал)
        String voltage, // Напряжение (220В, 380В)
        String country,
        Map<String, String> specifications,
        LocalDateTime createdAt,
        Integer categoryId,
        Integer quantity,
        ProductType productType
) {
    public CreateProductDto {
        if (priceProductStatus == null) {
            priceProductStatus = PriceProductStatus.CHECK_PRICE;
        }
    }
}
