package i.f.industrialfurniture.dto.user;

import java.time.LocalDateTime;

public record UserNewsDto(
        Integer newsId,
        String name,
        String newsPhotoUrl,
        LocalDateTime createDateNews
) {
}
