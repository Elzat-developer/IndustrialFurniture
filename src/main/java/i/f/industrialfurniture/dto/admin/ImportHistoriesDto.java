package i.f.industrialfurniture.dto.admin;

import i.f.industrialfurniture.model.ImportStatus;

import java.time.LocalDateTime;

public record ImportHistoriesDto(
        Integer id,
        String fileName,
        ImportStatus importStatus,
        LocalDateTime createdAt
) {
}
