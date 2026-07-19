package i.f.industrialfurniture.repositories;

import i.f.industrialfurniture.model.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order,Integer> {
    // Подтягиваем только "items", так как связей с другими таблицами внутри OrderItem нет
    @EntityGraph(attributePaths = {"items"})
    List<Order> findAllByCustomerPhoneOrderByOrderStartDateDesc(String phone);
}