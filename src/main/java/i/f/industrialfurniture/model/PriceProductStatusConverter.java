package i.f.industrialfurniture.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PriceProductStatusConverter implements AttributeConverter<PriceProductStatus, String> {

    @Override
    public String convertToDatabaseColumn(PriceProductStatus attribute) {
        return attribute != null ? attribute.value() : PriceProductStatus.CHECK_PRICE_VALUE;
    }

    @Override
    public PriceProductStatus convertToEntityAttribute(String dbData) {
        return PriceProductStatus.of(dbData);
    }
}
