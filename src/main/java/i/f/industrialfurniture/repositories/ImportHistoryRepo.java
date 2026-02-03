package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.entity.ImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportHistoryRepo extends JpaRepository<ImportHistory, Integer> {
}
