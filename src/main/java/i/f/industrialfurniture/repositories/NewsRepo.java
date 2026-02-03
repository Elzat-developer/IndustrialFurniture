package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepo extends JpaRepository<News,Integer> {
}
