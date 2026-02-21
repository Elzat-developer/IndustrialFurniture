package i.f.industrialfurniture.dto.admin;

import i.f.industrialfurniture.model.ImportStatus;

import java.time.LocalDateTime;

public record ImportHistoryDto(
        Integer id,
        String fileName,
        Integer successCount,
        Integer errorCount,
        ImportStatus importStatus,
        String errorsLog,
        LocalDateTime createdAt
) {
}
