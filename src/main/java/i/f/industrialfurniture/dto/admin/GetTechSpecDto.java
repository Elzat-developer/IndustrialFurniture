package i.f.industrialfurniture.dto.admin;

public record GetTechSpecDto(
        Integer techSpecId,
        String fileName,
        String fileUrl,
        Integer product_id
) {
}
