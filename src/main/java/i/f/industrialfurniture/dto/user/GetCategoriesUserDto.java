package i.f.industrialfurniture.dto.user;

public record GetCategoriesUserDto(
        Integer categoryId,
        String categoryName,
        String photoUrl
) {
}
