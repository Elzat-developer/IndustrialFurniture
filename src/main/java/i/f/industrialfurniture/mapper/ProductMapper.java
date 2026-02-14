package i.f.industrialfurniture.mapper;

import i.f.industrialfurniture.dto.admin.EditProductDto;
import i.f.industrialfurniture.dto.user.CreateProductDto;
import i.f.industrialfurniture.model.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE // Чтобы не ругался на лишние поля
)
public interface ProductMapper {
    // ОБНОВЛЕНИЕ
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "specifications", ignore = true) // Игнорируем, обновим сами
    void updateProductFromDto(EditProductDto dto, @MappingTarget Product entity);
    // СОЗДАНИЕ
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "photos", ignore = true)
    // Если в DTO поле называется specifications, то эта строка не обязательна, но полезна для ясности
    @Mapping(target = "specifications", source = "specifications")
    Product createProductFromDto(CreateProductDto dto);
}
