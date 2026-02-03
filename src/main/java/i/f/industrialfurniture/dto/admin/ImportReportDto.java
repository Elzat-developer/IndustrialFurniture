package i.f.industrialfurniture.dto.admin;

import java.util.List;

public record ImportReportDto(
        int successCount,
        int errorCount,
        List<String> errorMessages
) {
}
