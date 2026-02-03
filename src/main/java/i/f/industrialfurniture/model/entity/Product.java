package i.f.industrialfurniture.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "product_name")
    private String productName;
    private String description;
    private String tag;//артикуль
    private BigDecimal price;
    private Integer quantity;
    private String material;
    private String dimensions;
    private Double weight;
    @Column(name = "active", nullable = false)
    private boolean active = true;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @OneToOne
    @JoinColumn(name = "category_id")
    private Category category;
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    @OrderColumn(name = "photo_order_index")
//    private List<ProductImage> photos = new ArrayList<>();
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
    @ManyToMany(mappedBy = "products")
    private List<Order> orders;
}
