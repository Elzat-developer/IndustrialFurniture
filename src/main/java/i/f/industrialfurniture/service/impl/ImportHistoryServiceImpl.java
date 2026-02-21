package i.f.industrialfurniture.service.impl;

import i.f.industrialfurniture.model.ImportStatus;
import i.f.industrialfurniture.model.entity.ImportHistory;
import i.f.industrialfurniture.repositories.ImportHistoryRepo;
import i.f.industrialfurniture.service.ImportHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportHistoryServiceImpl implements ImportHistoryService {
    private final ImportHistoryRepo importHistoryRepo;
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveImportHistory(String fileName, int success, List<String> errors) {
        ImportHistory history = new ImportHistory();
        history.setFileName(fileName);
        history.setSuccessCount(success);
        history.setErrorCount(errors.size());

        // Определяем статус
        if (errors.isEmpty()) {
            history.setImportStatus(ImportStatus.SUCCESS);
        } else if (success > 0) {
            history.setImportStatus(ImportStatus.PARTIAL);
        } else {
            history.setImportStatus(ImportStatus.FAILED);
        }

        // Ограничиваем лог ошибок, чтобы не переполнить колонку в БД (на всякий случай)
        String log = String.join("\n", errors);
        if (log.length() > 65000) { // для типа TEXT
            log = log.substring(0, 65000) + "... [truncated]";
        }

        history.setErrorsLog(log);
        history.setCreatedAt(LocalDateTime.now());

        importHistoryRepo.saveAndFlush(history);
    }
}
