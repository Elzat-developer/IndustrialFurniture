package i.f.industrialfurniture.dto.user;

public record DetailedOrderProductDTO(
        Integer product_id,
        String name,
        String description,
        Integer price,
        Integer oldPrice,
        Boolean active,
        String catalogName, // Предполагаем, что вам нужно только имя каталога
        ProductPhotoDTO photo
) {
}
