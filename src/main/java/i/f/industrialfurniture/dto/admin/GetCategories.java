package i.f.industrialfurniture.dto.admin;

public record GetCategories(
        Integer categoryId,
        String categoryName,
        String description,
        String photoUrl
) {
}
