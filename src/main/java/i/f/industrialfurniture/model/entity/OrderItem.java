package i.f.industrialfurniture.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int quantity;
    @Column(name = "product_name")
    private String productName;
    private String tag;//артикуль
    @Column(name = "price_at_purchase")
    private BigDecimal priceAtPurchase;
    @ManyToOne
    @JoinColumn(name = "orders_id",referencedColumnName = "id")
    private Order order;
}