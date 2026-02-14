package i.f.industrialfurniture.model.entity;

import i.f.industrialfurniture.model.CategoryType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Column(name = "category_name")
    private String categoryName;
    private String description;
    @Column(name = "photo_url")
    private String photoUrl;
    @Column(name = "category_type")
    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
