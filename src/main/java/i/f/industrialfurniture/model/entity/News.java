package i.f.industrialfurniture.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    private String name;
    private String description;
    @Column(name = "news_photo_url")
    private String newsPhotoUrl;
    @Column(name = "create_date_news")
    private LocalDateTime createDateNews;
}
