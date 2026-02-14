package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.CategoryType;
import i.f.industrialfurniture.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepo extends JpaRepository<Category,Integer>, JpaSpecificationExecutor<Category> {
    List<Category> findAllByCategoryType(CategoryType categoryType);
}
