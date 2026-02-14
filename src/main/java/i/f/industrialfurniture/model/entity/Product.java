package i.f.industrialfurniture.model.entity;

import i.f.industrialfurniture.model.ProductType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Double weight; // Вес (без упаковки), кг
    private Integer width;  // Ширина, мм
    private Integer depth;  // Глубина, мм
    private Integer height; // Высота, мм

    private String power;   // Мощность (может быть в кВт или ккал)
    private String voltage; // Напряжение (220В, 380В)

    private String country; // Страна производства

    // --- ДИНАМИЧЕСКИЕ ХАРАКТЕРИСТИКИ ---
    // Сюда пойдут: "Тип", "Количество зон нагрева", "Температурный режим" и т.д.

    @ElementCollection
    @CollectionTable(name = "product_specifications",
            joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "spec_name")
    @Column(name = "spec_value")
    private Map<String, String> specifications = new HashMap<>();

    @Column(name = "active", nullable = false)
    private boolean active = true;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "product_type")
    @Enumerated(EnumType.STRING)
    private ProductType productType;
    @OneToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> photos = new ArrayList<>();
}
