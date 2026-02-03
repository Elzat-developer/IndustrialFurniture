package i.f.industrialfurniture.entity;

import i.f.industrialfurniture.model.PaidStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "order_number")
    private String orderNumber;
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "customer_phone")
    private String customerPhone;
    @Column(name = "order_start_date")
    private LocalDateTime orderStartDate;
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    @Column(name = "paid_status")
    @Enumerated(EnumType.STRING)
    private PaidStatus paidStatus;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
}
