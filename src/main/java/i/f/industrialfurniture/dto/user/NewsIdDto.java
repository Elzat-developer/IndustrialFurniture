package i.f.industrialfurniture.dto.user;

import java.time.LocalDateTime;

public record NewsIdDto(
        Integer newsId,
        String name,
        String description,
        String newsPhotoUrl,
        LocalDateTime dateTime
) {
}
