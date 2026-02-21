package i.f.industrialfurniture.service;

import java.util.List;

public interface ImportHistoryService {
    void saveImportHistory(String originalFilename, int successCount, List<String> errors);
}
